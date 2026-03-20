package com.coding.distributed_lovable.Intelligent_service.service.impl;

import com.coding.distributed_lovable.Intelligent_service.client.WorkspaceClient;
import com.coding.distributed_lovable.Intelligent_service.dto.StreamResponse;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatEvent;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatMessage;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatSession;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatSessionId;
import com.coding.distributed_lovable.Intelligent_service.llm.LlmResponseParser;
import com.coding.distributed_lovable.Intelligent_service.llm.PromptUtils;
import com.coding.distributed_lovable.Intelligent_service.llm.advisors.FileTreeContextAdvisor;
import com.coding.distributed_lovable.Intelligent_service.llm.tools.CodeGenerationTools;
import com.coding.distributed_lovable.Intelligent_service.repository.ChatEventRepository;
import com.coding.distributed_lovable.Intelligent_service.repository.ChatMessageRepository;
import com.coding.distributed_lovable.Intelligent_service.repository.ChatSessionRepository;
import com.coding.distributed_lovable.Intelligent_service.service.AIGenerationService;
import com.coding.distributed_lovable.Intelligent_service.service.UsageService;
import com.coding.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.coding.distributed_lovable.common_lib.enums.ChatEventType;
import com.coding.distributed_lovable.common_lib.enums.MessageRole;
import com.coding.distributed_lovable.common_lib.event.FileStoreRequestEvent;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIGenerationServiceImpl implements AIGenerationService {
  private final ChatClient chatClient;
  private final AuthUtil authUtil;
  private final FileTreeContextAdvisor fileTreeContextAdvisor;
  private final LlmResponseParser llmResponseParser;
  private final ChatSessionRepository chatSessionRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatEventRepository chatEventRepository;
  private final UsageService usageService;
  private final WorkspaceClient workspaceClient;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static final Pattern FILE_TAG_PATTERN =
      Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>", Pattern.DOTALL);

  @Override
  @PreAuthorize("@security.canEditProject(#projectId)")
  public Flux<StreamResponse> streamResponse(String userMessage, Long projectId) {

    // usageService.checkDailyTokenUsage();

    Long userId = authUtil.getCurrentUserId();

    ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

    Map<String, Object> advisorParams = Map.of("userId", userId, "projectId", projectId);

    CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectId, workspaceClient);

    AtomicReference<Long> startTime = new AtomicReference<>(0L);
    AtomicReference<Long> endTime = new AtomicReference<>(0L);
    AtomicReference<Usage> usageRef = new AtomicReference<>();

    StringBuilder fullResponseBuffer = new StringBuilder();

    return chatClient
        .prompt()
        // .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
        .user(
            "INSTRUCTIONS: "
                + PromptUtils.CODE_GENERATION_SYSTEM_PROMPT
                + "\n\nUSER REQUEST: "
                + userMessage)
        .tools(codeGenerationTools)
        .advisors(
            advisorspec -> {
              advisorspec.params(advisorParams);
              advisorspec.advisors(fileTreeContextAdvisor);
            })
        .stream()
        .chatResponse()
        .doOnNext(
            response -> {
              String content = response.getResult().getOutput().getText();
              if (content != null
                  && !content.isEmpty()
                  && endTime.get() == 0) { // first non-empty chunk
                endTime.set(System.currentTimeMillis());
              }

              if (response.getMetadata().getUsage() != null) {
                usageRef.set(response.getMetadata().getUsage());
              }

              fullResponseBuffer.append(content);
            })
        .doOnComplete(
            () -> {
              Schedulers.boundedElastic()
                  .schedule(
                      () -> {
                        // parseAndSaveFiles(fullResponseBuffer.toString(), projectId, userId);
                        long duration = (endTime.get() - startTime.get()) / 1000;
                        finalizeChats(
                            userMessage,
                            chatSession,
                            fullResponseBuffer.toString(),
                            duration,
                            usageRef.get());
                      });
            })
        .doOnError(error -> log.error("Error encountered during chat streaming", error))
        .map(
            response -> {
              String text = Objects.requireNonNull(response.getResult().getOutput().getText());
              return new StreamResponse(text != null ? text : "");
            });
  }

  private void finalizeChats(
      String userMessage, ChatSession chatSession, String fullText, long duration, Usage usage) {
    Long projectId = chatSession.getId().getProjectId();

    // record usage tokens
    if (usage != null) {
      int totalTokens = usage.getTotalTokens();
      usageService.recordTokenUsage(chatSession.getId().getUserId(), totalTokens);
    }

    // Save the user message
    chatMessageRepository.save(
        ChatMessage.builder()
            .chatSession(chatSession)
            .role(MessageRole.USER)
            .content(userMessage)
            .tokensUsed(usage.getPromptTokens())
            .build());

    // Save assistant chat message
    ChatMessage assistantChatMessage =
        ChatMessage.builder()
            .role(MessageRole.ASSISTANT)
            .chatSession(chatSession)
            .tokensUsed(usage.getCompletionTokens())
            .content("Assistant chat message...")
            .build();

    assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

    List<ChatEvent> chatEventList =
        llmResponseParser.parseChatEvents(fullText, assistantChatMessage);
    chatEventList.add(
        0,
        ChatEvent.builder()
            .type(ChatEventType.THOUGHT)
            .status(ChatEventStatus.CONFIRMED)
            .chatMessage(assistantChatMessage)
            .content("Thought for " + duration + "s")
            .sequenceOrder(0)
            .build());
    chatEventList.stream()
        .filter(event -> event.getType() == ChatEventType.FILE_EDIT)
        .forEach(
            e -> {
              //     projectFileService.saveFile(projectId, e.getFilePath(), e.getContent()); TODO:
              // We have to use Kafka
              String sagaId = UUID.randomUUID().toString();
              FileStoreRequestEvent fileStoreRequestEvent =
                  new FileStoreRequestEvent(
                      projectId,
                      sagaId,
                      e.getFilePath(),
                      e.getContent(),
                      chatSession.getId().getUserId());
              log.info("Storage request event sent: {}", e.getFilePath());
              kafkaTemplate.send(
                  "file-storage-request-event", "project-" + projectId, fileStoreRequestEvent);
            });

    chatEventRepository.saveAll(chatEventList);
  }

  private void parseAndSaveFiles(String fullResponse, Long projectId, Long userId) {
    String dummy =
        """
        <message> I'm going to read the files and generate the code </message>
        <file path="src/App.jsx">
            import App from './App.jsx'
        </file>

        <message> I'm going to read the files and generate the code </message>
        <file path="src/App.jsx">
            import App from './App.jsx'
        </file>
        """;
    Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
    while (matcher.find()) {
      String filePath = matcher.group(1);
      String fileContent = matcher.group(2).trim();
      //    projectFileService.saveFile(projectId, filePath, fileContent);  TODO: We have to use
      // Kafka
      FileStoreRequestEvent fileStoreRequestEvent =
          new FileStoreRequestEvent(projectId, "", filePath, fileContent, userId);
      kafkaTemplate.send(
          "file-storage-request-event", "project-" + projectId, fileStoreRequestEvent);
    }
  }

  private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
    ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
    ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
    if (chatSession == null) {
      chatSession = ChatSession.builder().id(chatSessionId).build();
      chatSession = chatSessionRepository.save(chatSession);
    }
    return chatSession;
  }
}

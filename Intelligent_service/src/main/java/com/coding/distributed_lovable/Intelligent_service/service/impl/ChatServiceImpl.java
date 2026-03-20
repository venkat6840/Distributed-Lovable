package com.coding.distributed_lovable.Intelligent_service.service.impl;

import com.coding.distributed_lovable.Intelligent_service.dto.ChatResponse;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatMessage;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatSession;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatSessionId;
import com.coding.distributed_lovable.Intelligent_service.mapper.ChatMapper;
import com.coding.distributed_lovable.Intelligent_service.repository.ChatMessageRepository;
import com.coding.distributed_lovable.Intelligent_service.repository.ChatSessionRepository;
import com.coding.distributed_lovable.Intelligent_service.service.ChatService;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatMessageRepository chatMessageRepository;
  private final AuthUtil authUtil;
  private final ChatSessionRepository chatSessionRepository;
  private final ChatMapper chatMapper;

  @Override
  public List<ChatResponse> getProjectChatHistory(Long projectId) {
    Long userId = authUtil.getCurrentUserId();
    ChatSession chatSession =
        chatSessionRepository.getReferenceById(new ChatSessionId(projectId, userId));

    List<ChatMessage> chatMessagesList = chatMessageRepository.findByChatSession(chatSession);
    return chatMapper.fromListOfChatMessage(chatMessagesList);
  }
}

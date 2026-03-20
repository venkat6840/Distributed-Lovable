package com.coding.distributed_lovable.Intelligent_service.controller;

import com.coding.distributed_lovable.Intelligent_service.dto.ChatRequest;
import com.coding.distributed_lovable.Intelligent_service.dto.ChatResponse;
import com.coding.distributed_lovable.Intelligent_service.dto.StreamResponse;
import com.coding.distributed_lovable.Intelligent_service.service.AIGenerationService;
import com.coding.distributed_lovable.Intelligent_service.service.ChatService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChatController {

  AIGenerationService aiGenerationService;
  ChatService chatService;

  @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<StreamResponse>> streamChat(@RequestBody ChatRequest chatRequest) {
    return aiGenerationService
        .streamResponse(chatRequest.message(), chatRequest.projectId())
        .map(data -> ServerSentEvent.<StreamResponse>builder().data(data).build());
  }

  @GetMapping(value = "/chat/projects/{projectId}")
  public ResponseEntity<List<ChatResponse>> getChatHistory(@PathVariable Long projectId) {
    return ResponseEntity.ok(chatService.getProjectChatHistory(projectId));
  }
}

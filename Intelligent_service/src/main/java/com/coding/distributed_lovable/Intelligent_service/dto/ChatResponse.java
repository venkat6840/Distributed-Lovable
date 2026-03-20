package com.coding.distributed_lovable.Intelligent_service.dto;

import com.coding.distributed_lovable.Intelligent_service.entity.ChatSession;
import com.coding.distributed_lovable.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
    Long id,
    ChatSession chatSession,
    MessageRole role, // User or Assistant
    List<ChatEventResponse> events,
    String content,
    Integer tokensUsed,
    Instant createdAt) {}

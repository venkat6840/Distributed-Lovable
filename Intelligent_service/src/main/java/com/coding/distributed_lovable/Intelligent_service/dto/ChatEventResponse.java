package com.coding.distributed_lovable.Intelligent_service.dto;

import com.coding.distributed_lovable.common_lib.enums.ChatEventType;

public record ChatEventResponse(
    Long id,
    Integer sequenceOrder,
    ChatEventType type,
    String content,
    String filePath,
    String metadata) {}

package com.coding.distributed_lovable.Intelligent_service.service;

import com.coding.distributed_lovable.Intelligent_service.dto.ChatResponse;

import java.util.List;

public interface ChatService {
  List<ChatResponse> getProjectChatHistory(Long projectId);
}

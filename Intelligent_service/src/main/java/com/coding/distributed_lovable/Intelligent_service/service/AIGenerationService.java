package com.coding.distributed_lovable.Intelligent_service.service;

import com.coding.distributed_lovable.Intelligent_service.dto.StreamResponse;
import reactor.core.publisher.Flux;

public interface AIGenerationService {

  Flux<StreamResponse> streamResponse(String userMessage, Long projectId);
}

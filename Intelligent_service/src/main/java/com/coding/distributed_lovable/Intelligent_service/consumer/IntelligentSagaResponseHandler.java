package com.coding.distributed_lovable.Intelligent_service.consumer;

import com.coding.distributed_lovable.Intelligent_service.repository.ChatEventRepository;
import com.coding.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.coding.distributed_lovable.common_lib.event.FileStoreResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IntelligentSagaResponseHandler {

  private final ChatEventRepository chatEventRepository;

  @Transactional
  @KafkaListener(topics = "file-store-responses", groupId = "intelligence-group")
  public void handleSagaResponse(FileStoreResponseEvent responseEvent) {

    chatEventRepository
        .findBySagaId(responseEvent.sagaId())
        .ifPresent(
            event -> {
              if (!ChatEventStatus.PENDING.equals(event.getStatus())) {
                log.info("Response is already handled : {}. skipping.", responseEvent.sagaId());
                return;
              }
              if (responseEvent.success()) {
                event.setStatus(ChatEventStatus.CONFIRMED);
                log.info("Saga {} CONFIRMED", responseEvent.sagaId());
              } else {
                log.warn("Saga {} failed. Deleting Event. ", responseEvent.sagaId());
                event.setStatus(ChatEventStatus.FAILED);
              }
            });
  }
}

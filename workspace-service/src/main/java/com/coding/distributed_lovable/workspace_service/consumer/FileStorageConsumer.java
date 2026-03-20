package com.coding.distributed_lovable.workspace_service.consumer;

import com.coding.distributed_lovable.common_lib.event.FileStoreRequestEvent;
import com.coding.distributed_lovable.common_lib.event.FileStoreResponseEvent;
import com.coding.distributed_lovable.workspace_service.entity.ProcessedEvent;
import com.coding.distributed_lovable.workspace_service.repository.ProcessedEventRepository;
import com.coding.distributed_lovable.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

  private final ProjectFileService projectFileService;
  private final ProcessedEventRepository processedEventRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Transactional
  @KafkaListener(topics = "file-storage-request-event", groupId = "workspace-group")
  public void consumeClient(FileStoreRequestEvent fileStoreRequestEvent) {

    if (processedEventRepository.existsById(fileStoreRequestEvent.sagaId())) {
      log.info(
          "Duplicate Saga detected : {}. Resending previous ACK.", fileStoreRequestEvent.sagaId());
      sendResponse(fileStoreRequestEvent, true, null);
      return;
    }

    try {
      log.info("Saving file: {}", fileStoreRequestEvent.filepath());
      projectFileService.saveFile(
          fileStoreRequestEvent.projectId(),
          fileStoreRequestEvent.filepath(),
          fileStoreRequestEvent.content());
      processedEventRepository.save(
          new ProcessedEvent(fileStoreRequestEvent.sagaId(), LocalDateTime.now()));
      sendResponse(fileStoreRequestEvent, true, null);
    } catch (Exception e) {
      log.error("Error saving the file : {}", e.getMessage());
      sendResponse(fileStoreRequestEvent, false, e.getMessage());
    }
  }

  private void sendResponse(
      FileStoreRequestEvent fileStoreRequestEvent, boolean success, String error) {
    FileStoreResponseEvent responseEvent =
        FileStoreResponseEvent.builder()
            .success(success)
            .sagaId(fileStoreRequestEvent.sagaId())
            .projectId(fileStoreRequestEvent.projectId())
            .errorMessage(error)
            .build();
    kafkaTemplate.send("file-store-responses", responseEvent);
  }
}

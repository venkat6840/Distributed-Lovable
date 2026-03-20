package com.coding.distributed_lovable.Intelligent_service.repository;

import com.coding.distributed_lovable.Intelligent_service.entity.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatEventRepository extends JpaRepository<ChatEvent, Long> {

  Optional<ChatEvent> findBySagaId(String sagaId);
}

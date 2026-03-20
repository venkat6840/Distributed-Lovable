package com.coding.distributed_lovable.Intelligent_service.repository;

import com.coding.distributed_lovable.Intelligent_service.entity.ChatSession;
import com.coding.distributed_lovable.Intelligent_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {}

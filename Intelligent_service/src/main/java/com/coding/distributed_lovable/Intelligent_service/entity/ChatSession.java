package com.coding.distributed_lovable.Intelligent_service.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @Author - Venkatesh G
 */
@Getter
@Setter
@Entity
@Table(name = "chat_sessions")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatSession {

  @EmbeddedId ChatSessionId id;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  Instant createdAt;

  @UpdateTimestamp Instant updatedAt;

  Instant deletedAt; // soft delete
}

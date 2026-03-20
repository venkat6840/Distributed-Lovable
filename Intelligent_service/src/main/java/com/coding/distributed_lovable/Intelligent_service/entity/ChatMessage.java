package com.coding.distributed_lovable.Intelligent_service.entity;

import com.coding.distributed_lovable.common_lib.enums.MessageRole;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

/**
 * @Author - Venkatesh G
 */
@Getter
@Setter
@Entity
@Table(name = "chat_messages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  ChatSession chatSession;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  MessageRole role; // User or Assistant

  @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @OrderBy("sequenceOrder ASC")
  List<ChatEvent> events;

  @Column(columnDefinition = "text")
  String content;

  Integer tokensUsed = 0;

  @CreationTimestamp Instant createdAt;
}

package com.coding.distributed_lovable.Intelligent_service.entity;

import com.coding.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.coding.distributed_lovable.common_lib.enums.ChatEventType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "chat_events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false)
  ChatMessage chatMessage;

  @Column(nullable = false)
  Integer sequenceOrder;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  ChatEventType type;

  @Column(columnDefinition = "text")
  String content;

  String filePath;

  @Column(columnDefinition = "text")
  String metadata;

  String sagaId;

  @Enumerated(EnumType.STRING)
  //@Column(nullable = false)
  ChatEventStatus status;
}

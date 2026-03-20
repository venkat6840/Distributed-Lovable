package com.coding.distributed_lovable.Intelligent_service.entity;

import java.io.Serializable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class ChatSessionId implements Serializable {
  Long projectId;
  Long userId;
}

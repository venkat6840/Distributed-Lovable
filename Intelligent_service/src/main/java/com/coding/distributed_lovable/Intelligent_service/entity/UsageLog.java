package com.coding.distributed_lovable.Intelligent_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @Author - Venkatesh G
 */
@Getter
@Setter
@Entity
@Table(
    name = "usage_logs",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"user_id", "date"}) // one log per user per day
    })
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @Column(name = "user_id", nullable = false)
  Long userId;

  @Column(name = "date", nullable = false)
  LocalDate date;

  Integer tokensUsed;
}

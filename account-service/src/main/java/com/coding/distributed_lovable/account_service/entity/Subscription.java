package com.coding.distributed_lovable.account_service.entity;

import com.coding.distributed_lovable.common_lib.enums.SubscriptionStatus;
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
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "user_id")
  User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "plan_id")
  Plan plan;

  @Enumerated(value = EnumType.STRING)
  SubscriptionStatus status;

  String stripeSubscriptionId; // we can rename to gatewaySubscriptionId

  Instant currentPeriodStart;
  Instant currentPeriodEnd;
  Boolean cancelAtPeriodEnd = false;

  @CreationTimestamp Instant createdAt;

  @UpdateTimestamp Instant updatedAt;
}

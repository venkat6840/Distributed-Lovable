package com.coding.distributed_lovable.account_service.repository;

import java.util.Optional;
import java.util.Set;

import com.coding.distributed_lovable.account_service.entity.Subscription;
import com.coding.distributed_lovable.common_lib.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
  /**
   * Get the current active subscription
   *
   * @param userId
   * @param status
   * @return
   */
  Optional<Subscription> findByUserIdAndStatusIn(Long userId, Set<SubscriptionStatus> status);

  boolean existsByStripeSubscriptionId(String stripeSubscriptionId);

  Optional<Subscription> findByStripeSubscriptionId(String gatewaySubscriptionId);
}

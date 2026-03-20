package com.coding.distributed_lovable.account_service.service;

import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutRequest;
import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
  SubscriptionResponse getCurrentSubscription();

  CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

  PortalResponse openCustomerPortal();

  void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

  void updateSubscription(
      String gatewaySubscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Boolean cancelAtPeriodEnd,
      Long planId);

  void cancelSubscription(String gatewaySubscriptionId);

  void renewSubscriptionPeriod(
      String gatewaySubscriptionId, Instant periodStart, Instant periodEnd);

  PortalResponse openCustomerPortal(Long userId);

  void markSubscriptionPastDue(String gatewaySubscriptionId);

  PlanDto getCurrentSubscribedPlanByUser();

}

package com.coding.distributed_lovable.account_service.service.impl;

import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutRequest;
import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.SubscriptionResponse;
import com.coding.distributed_lovable.account_service.entity.Plan;
import com.coding.distributed_lovable.account_service.entity.Subscription;
import com.coding.distributed_lovable.account_service.entity.User;
import com.coding.distributed_lovable.account_service.mapper.SubscriptionResponseMapper;
import com.coding.distributed_lovable.account_service.repository.PlanRepository;
import com.coding.distributed_lovable.account_service.repository.SubscriptionRepository;
import com.coding.distributed_lovable.account_service.repository.UserRepository;
import com.coding.distributed_lovable.account_service.service.SubscriptionService;
import com.coding.distributed_lovable.common_lib.dto.PlanDto;
import com.coding.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {
  private final AuthUtil authUtil;
  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionResponseMapper subscriptionResponseMapper;
  private final UserRepository userRepository;
  private final PlanRepository planRepository;
  private static final int FREE_TIER_ALLOWED_PROJECTS = 100;

  @Override
  public SubscriptionResponse getCurrentSubscription() {
    Long userId = authUtil.getCurrentUserId();

    var subscription =
        subscriptionRepository
            .findByUserIdAndStatusIn(
                userId,
                Set.of(
                    SubscriptionStatus.ACTIVE,
                    SubscriptionStatus.PAST_DUE,
                    SubscriptionStatus.TRAILING))
            .orElse(new Subscription());
    return subscriptionResponseMapper.toSubscriptionResponseFromSubscription(subscription);
  }

  @Override
  public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
    Long currentUserId = authUtil.getCurrentUserId();
    return null;
  }

  @Override
  public PortalResponse openCustomerPortal() {
    return null;
  }

  @Override
  public void activateSubscription(
      Long userId, Long planId, String subscriptionId, String customerId) {
    boolean subscriptionExists =
        subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
    if (subscriptionExists) return;

    User user = getUser(userId);
    Plan plan = getPlan(planId);

    Subscription subscription =
        Subscription.builder()
            .user(user)
            .plan(plan)
            .stripeSubscriptionId(subscriptionId)
            .status(SubscriptionStatus.INCOMPLETE)
            .build();
    subscriptionRepository.save(subscription);
  }

  @Override
  @Transactional
  public void updateSubscription(
      String gatewaySubscriptionId,
      SubscriptionStatus status,
      Instant periodStart,
      Instant periodEnd,
      Boolean cancelAtPeriodEnd,
      Long planId) {

    Subscription subscription = getSubscription(gatewaySubscriptionId);
    boolean hasSubscriptionUpdated = false;
    if (status != null && status != subscription.getStatus()) {
      subscription.setStatus(status);
      hasSubscriptionUpdated = true;
    }

    if (periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())) {
      subscription.setCurrentPeriodStart(periodStart);
      hasSubscriptionUpdated = true;
    }

    if (periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())) {
      subscription.setCurrentPeriodEnd(periodEnd);
      hasSubscriptionUpdated = true;
    }

    if (cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()) {
      subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
      hasSubscriptionUpdated = true;
    }

    if (planId != null && !planId.equals(subscription.getPlan().getId())) {
      Plan plan = getPlan(planId);
      subscription.setPlan(plan);
      hasSubscriptionUpdated = true;
    }

    if (hasSubscriptionUpdated) {
      log.debug("Subscription have been updated: {}", hasSubscriptionUpdated);
      subscriptionRepository.save(subscription);
    }
  }

  @Override
  public void cancelSubscription(String gatewaySubscriptionId) {
    Subscription subscription = getSubscription(gatewaySubscriptionId);
    if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
      log.debug(
          "Subscription is already cancelled, gatewaySubscriptionId - {}", gatewaySubscriptionId);
      return;
    }
    subscription.setStatus(SubscriptionStatus.CANCELLED);
    subscriptionRepository.save(subscription);
  }

  @Override
  public void renewSubscriptionPeriod(
      String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {
    Subscription subscription = getSubscription(gatewaySubscriptionId);

    Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodStart();
    subscription.setCurrentPeriodStart(newStart);

    Instant newEnd = periodEnd != null ? periodEnd : subscription.getCurrentPeriodEnd();
    subscription.setCurrentPeriodEnd(newEnd);

    if (subscription.getStatus() == SubscriptionStatus.INCOMPLETE
        || subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
      subscription.setStatus(SubscriptionStatus.ACTIVE);
    }

    subscriptionRepository.save(subscription);
  }

  private Subscription getSubscription(String gatewaySubscriptionId) {
    return subscriptionRepository
        .findByStripeSubscriptionId(gatewaySubscriptionId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Stripe Subscription not found with stripeId : {}", gatewaySubscriptionId));
  }

  @Override
  public PortalResponse openCustomerPortal(Long userId) {
    return null;
  }

  @Override
  public void markSubscriptionPastDue(String gatewaySubscriptionId) {

    Subscription subscription = getSubscription(gatewaySubscriptionId);
    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
      log.debug(
          "Subscription is already past due, gatewaySubscriptionId - {}", gatewaySubscriptionId);
      return;
    }
    subscription.setStatus(SubscriptionStatus.PAST_DUE);
    subscriptionRepository.save(subscription);
  }

  @Override
  public PlanDto getCurrentSubscribedPlanByUser() {
    SubscriptionResponse subscriptionResponse = getCurrentSubscription();
    return subscriptionResponse.plan();
  }

  // Utility methods

  private User getUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with Id: ", userId.toString()));
    return user;
  }

  private Plan getPlan(Long planId) {
    Plan plan =
        planRepository
            .findById(planId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Plan not found with Id: ", planId.toString()));
    return plan;
  }
}

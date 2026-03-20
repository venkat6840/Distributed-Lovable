package com.coding.distributed_lovable.account_service.service.impl;

import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutRequest;
import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.coding.distributed_lovable.account_service.entity.Plan;
import com.coding.distributed_lovable.account_service.entity.User;
import com.coding.distributed_lovable.account_service.repository.PlanRepository;
import com.coding.distributed_lovable.account_service.repository.UserRepository;
import com.coding.distributed_lovable.account_service.service.PaymentProcessor;
import com.coding.distributed_lovable.account_service.service.SubscriptionService;
import com.coding.distributed_lovable.common_lib.enums.SubscriptionStatus;
import com.coding.distributed_lovable.common_lib.error.BadRequestException;
import com.coding.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.coding.distributed_lovable.common_lib.security.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {

  private static final Logger log = LoggerFactory.getLogger(StripePaymentProcessor.class);
  private final AuthUtil authUtil;
  private final PlanRepository planRepository;
  private final SubscriptionService subscriptionService;

  @Value("${app.frontend.url}")
  private String frontEndUrl;

  private final UserRepository userRepository;

  @Override
  public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
    Long userId = authUtil.getCurrentUserId();
    User user = getUser(userId);

    Plan plan =
        planRepository
            .findById(request.planId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Plan not found ", String.valueOf(request.planId())));
    // Create a checkout session
    var sessionCreateParams =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSubscriptionData(
                new SessionCreateParams.SubscriptionData.Builder()
                    .setBillingMode(
                        SessionCreateParams.SubscriptionData.BillingMode.builder()
                            .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                            .build())
                    .build())
            .setSuccessUrl(frontEndUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(frontEndUrl + "/cancel.html")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPrice(plan.getStripePriceId())
                    .setQuantity(1L)
                    .build())
            .putMetadata("user_id", userId.toString())
            .putMetadata("plan_id", plan.getId().toString());
    try {
      String stripeCustomerId = user.getStripeCustomerId();
      if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
        sessionCreateParams.setCustomerEmail(user.getName());
      } else {
        sessionCreateParams.setCustomer(stripeCustomerId); // stripe customer Id
      }
      Session session = Session.create(sessionCreateParams.build());
      return new CheckoutResponse(session.getUrl());
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PortalResponse openCustomerPortal() throws StripeException {
    Long userId = authUtil.getCurrentUserId();
    User user = getUser(userId);

    String stripeCustomerId = user.getStripeCustomerId();

    if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
      throw new BadRequestException("user does not have stripe customer, userID : " + userId);
    }

    var portalSession =
        com.stripe.model.billingportal.Session.create(
            com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(stripeCustomerId)
                .setReturnUrl(frontEndUrl)
                .build());

    return new PortalResponse(portalSession.getUrl());
  }

  @Override
  public void handleWebhookEvent(
      String type, StripeObject stripeObject, Map<String, String> metadata) throws StripeException {

    log.debug("Handling stripe event: {} ", type);

    switch (type) {
      case "checkout.session.completed" ->
          handleCheckoutSessionCompleted((Session) stripeObject, metadata);
      case "customer.subscription.updated" ->
          handleCustomerSubscriptionUpdated((Subscription) stripeObject);
      case "customer.subscription.deleted" ->
          handleCustomerSubscriptionDeleted((Subscription) stripeObject);
      case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
      case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
      default -> log.debug("Ignoring the event type: {} ", type);
    }
  }

  private void handleInvoicePaymentFailed(Invoice invoice) {
    String subscriptionId = extractSubscriptionId(invoice);
    if (subscriptionId == null) {
      log.error("subscription Id is null in the in");
      return;
    }
    subscriptionService.markSubscriptionPastDue(subscriptionId);
  }

  private void handleInvoicePaid(Invoice invoice) throws StripeException {
    String subscriptionId = extractSubscriptionId(invoice);
    if (subscriptionId == null) {
      log.error("subscription Id is null");
      return;
    }

    Subscription subscription = Subscription.retrieve(subscriptionId); // sdk calling stripe server
    SubscriptionItem item = subscription.getItems().getData().get(0);
    Instant periodStart = toInstant(item.getCurrentPeriodStart());
    Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

    subscriptionService.renewSubscriptionPeriod(subscriptionId, periodStart, periodEnd);
  }

  private void handleCustomerSubscriptionDeleted(Subscription subscription) {
    if (subscription == null) {
      log.error("subscription object in handle delete is null");
      return;
    }

    subscriptionService.cancelSubscription(subscription.getId());
  }

  private void handleCustomerSubscriptionUpdated(Subscription subscription) {
    if (subscription == null) {
      log.error("subscription object in handle update is null");
      return;
    }

    SubscriptionStatus status = mapStripeStatusToEnum(subscription.getStatus());
    if (status == null) {
      log.warn(
          "Unknown status '{}' for subscription {}",
          subscription.getStatus(),
          subscription.getId());
      return;
    }

    SubscriptionItem item = subscription.getItems().getData().get(0);
    Instant periodStart = toInstant(item.getCurrentPeriodStart());
    Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

    Long planId = resolvePlanID(item.getPrice());
    log.info("Cancel at : {} ", subscription.getCancelAt());
    log.info("Cancel at period end : {} ", subscription.getCancelAtPeriodEnd());
    subscriptionService.updateSubscription(
        subscription.getId(),
        status,
        periodStart,
        periodEnd,
        subscription.getCancelAt() != null,
        planId);
  }

  private Instant toInstant(Long epoch) {
    return epoch != null ? Instant.ofEpochSecond(epoch) : null;
  }

  private SubscriptionStatus mapStripeStatusToEnum(String status) {
    return switch (status) {
      case "active" -> SubscriptionStatus.ACTIVE;
      case "trialing" -> SubscriptionStatus.TRAILING;
      case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
      case "cancelled" -> SubscriptionStatus.CANCELLED;
      case "incomplete" -> SubscriptionStatus.INCOMPLETE;
      default -> {
        log.warn("Unmapped stripe status : {}", status);
        yield null;
      }
    };
  }

  private Long resolvePlanID(Price price) {
    if (price == null || price.getId() == null) return null;
    return planRepository.findByStripePriceId(price.getId()).map(Plan::getId).orElse(null);
  }

  private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
    if (session == null) {
      log.error("Session object was null");
      return;
    }
    Long userId = Long.parseLong(metadata.get("user_id"));
    Long planId = Long.parseLong(metadata.get("plan_id"));
    String subscriptionId = session.getSubscription();
    String customerId = session.getCustomer();

    User user = getUser(userId);

    if (user.getStripeCustomerId() == null) {
      user.setStripeCustomerId(customerId);
      userRepository.save(user);
    }

    subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
  }

  private User getUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with Id: ", userId.toString()));
    return user;
  }

  private String extractSubscriptionId(Invoice invoice) {
    var parent = invoice.getParent();
    if (parent == null) return null;

    var subDetails = parent.getSubscriptionDetails();
    if (subDetails == null) return null;

    return subDetails.getSubscription();
  }
}

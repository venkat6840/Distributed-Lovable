package com.coding.distributed_lovable.account_service.controller;

import com.coding.distributed_lovable.account_service.dto.subscription.*;
import com.coding.distributed_lovable.account_service.service.PaymentProcessor;
import com.coding.distributed_lovable.account_service.service.SubscriptionService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BillingController {
  private static final Logger log = LoggerFactory.getLogger(BillingController.class);
  private final SubscriptionService subscriptionService;
  private final PaymentProcessor paymentProcessor;

  @Value("${stripe.webhook.secret}")
  private String stripeSecretWebhook;

  @GetMapping("/api/me/subscription")
  public ResponseEntity<SubscriptionResponse> getMySubscription() {
    return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
  }

  @PostMapping("/api/payments/checkout")
  public ResponseEntity<CheckoutResponse> createCheckoutResponse(
      @RequestBody CheckoutRequest request) {
    Long userId = 1L;
    return ResponseEntity.ok(paymentProcessor.createCheckoutSessionUrl(request));
  }

  @PostMapping("/api/payments/portal")
  public ResponseEntity<PortalResponse> openCustomerPortal(Long userId) throws StripeException {
    return ResponseEntity.ok(paymentProcessor.openCustomerPortal());
  }

  @PostMapping("/webhooks/payment")
  public ResponseEntity<String> handlePaymentWebhook(
      @RequestBody String payload, HttpServletRequest request) {
    String signatureHeader = request.getHeader("Stripe-Signature");

    try {
      Event event = Webhook.constructEvent(payload, signatureHeader, stripeSecretWebhook);
      // Deserialize the nested object inside the event
      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      StripeObject stripeObject = null;
      if (dataObjectDeserializer.getObject().isPresent()) {
        stripeObject = dataObjectDeserializer.getObject().get();
      } else {
        // Deserialization failed, probably due to an API version mismatch.
        // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
        // instructions on how to handle this case, or return an error here.
        try {
          stripeObject = dataObjectDeserializer.deserializeUnsafe();
          if (stripeObject == null) {
            log.warn("Failed to deserialize webhook object for event: {}", event.getType());
            return ResponseEntity.ok().build();
          }
        } catch (Exception e) {
          log.error(
              "Unsafe deserialization failed for event {}: {}", event.getType(), e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body("Deserialization failed");
        }
      }

      // Now extract metadata only if it's a checkout session
      Map<String, String> metadata = new HashMap<>();
      if (stripeObject instanceof Session session) {
        metadata = session.getMetadata();
      }

      // pass to your processor
      paymentProcessor.handleWebhookEvent(event.getType(), stripeObject, metadata);
      return ResponseEntity.ok().build();

    } catch (SignatureVerificationException e) {
      throw new RuntimeException(e);
    } catch (StripeException e) {
      throw new RuntimeException(e);
    }
  }
}

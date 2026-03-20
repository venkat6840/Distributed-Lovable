package com.coding.distributed_lovable.account_service.service;

import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutRequest;
import com.coding.distributed_lovable.account_service.dto.subscription.CheckoutResponse;
import com.coding.distributed_lovable.account_service.dto.subscription.PortalResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import java.util.Map;

public interface PaymentProcessor {

  CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request);

  PortalResponse openCustomerPortal() throws StripeException;

  void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata)
      throws StripeException;
}

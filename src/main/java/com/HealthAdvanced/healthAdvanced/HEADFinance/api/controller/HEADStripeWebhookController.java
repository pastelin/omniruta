package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBusinessException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADStripeProperties;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.webhook.HEADStripePayoutWebhookService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.webhook.HEADStripeWebhookRouterService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe/webhooks")
public class HEADStripeWebhookController {

    private final HEADStripeWebhookRouterService stripeWebhookRouterService;
    private final HEADStripeProperties stripeProperties;

    @PostMapping("/connect")
    public ResponseEntity<String> handleConnectWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.webhook().secret());
            stripeWebhookRouterService.handle(event);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }
    }
}
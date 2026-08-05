package com.HealthAdvanced.healthAdvanced.HEADFinance.application.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.stripe.model.Event;

@Service
@RequiredArgsConstructor
public class HEADStripeWebhookRouterService {

    private final HEADStripePayoutWebhookService stripePayoutWebhookService;
    private final HEADStripeConnectedAccountWebhookService stripeConnectedAccountWebhookService;

    public void handle(Event event) {
        String type = event.getType();

        if (type == null) return;

        if (type.startsWith("payout.")) {
            stripePayoutWebhookService.handle(event);
            return;
        }

        if (type.startsWith("account.")
                || "person.updated".equals(type)) {
            stripeConnectedAccountWebhookService.handle(event);
        }
    }
}

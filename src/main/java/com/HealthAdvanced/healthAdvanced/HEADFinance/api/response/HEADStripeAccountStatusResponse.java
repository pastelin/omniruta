package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

public record HEADStripeAccountStatusResponse(
        String connectedAccountId,
        boolean detailsSubmitted,
        boolean payoutsEnabled,
        boolean chargesEnabled
) {}

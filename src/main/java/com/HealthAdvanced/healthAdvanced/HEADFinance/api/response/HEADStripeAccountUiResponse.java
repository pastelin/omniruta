package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountUiState;

public record HEADStripeAccountUiResponse(
        String connectedAccountId,
        HEADStripeAccountUiState uiState,

        String title,
        String subtitle,

        boolean detailsSubmitted,
        boolean payoutsEnabled,
        boolean chargesEnabled,

        boolean hasPayoutMethod,
        String payoutMethodType,     // BANK_ACCOUNT | DEBIT_CARD
        String bankName,
        String bankLast4,

        boolean showPrimaryButton,
        String primaryButtonText,
        String primaryAction,        // START_ONBOARDING | RESUME_ONBOARDING | CHECK_STATUS | VIEW_ACCOUNT

        boolean showSecondaryButton,
        String secondaryButtonText,
        String secondaryAction
) {}
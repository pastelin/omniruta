package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

public record HEADPaymentMethodResponse(
        String cardBrand,
        String lastFourDigits,
        Boolean isDefault
) {
}

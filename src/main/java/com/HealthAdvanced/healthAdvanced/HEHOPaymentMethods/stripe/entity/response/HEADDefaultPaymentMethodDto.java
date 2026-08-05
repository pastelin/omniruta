package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response;

public record HEADDefaultPaymentMethodDto(
        String id,
        String brand,
        String last4,
        boolean isDefault
) { }

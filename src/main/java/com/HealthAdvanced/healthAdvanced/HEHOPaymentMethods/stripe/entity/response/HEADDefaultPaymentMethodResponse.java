package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response;

public record HEADDefaultPaymentMethodResponse(
        boolean success,
        boolean hasMethod,
        HEADDefaultPaymentMethodDto method
) {
}

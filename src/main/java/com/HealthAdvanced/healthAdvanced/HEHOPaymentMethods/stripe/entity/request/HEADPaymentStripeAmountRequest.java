package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class HEADPaymentStripeAmountRequest {
    private double userLat;
    private double userLong;
    private String idPackage;
    private Long packageOptionId;
    private String paymentMethodId;
    private Long profileId;
}

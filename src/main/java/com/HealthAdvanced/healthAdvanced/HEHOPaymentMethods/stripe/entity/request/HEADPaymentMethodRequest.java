package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADPaymentMethodRequest {
    private String paymentMethodId;
    private Boolean setAsDefault;
}

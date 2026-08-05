package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.response;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import com.stripe.model.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADPaymentStripeResponse {
    private Long jobId;
    private Boolean success;
    private String clientSecret;
    private String paymentIntentId;
    private String stripeStatus;
    private HEADPaymentStatus paymentStatus;
}
package com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.Entity.Dto;

import lombok.Data;

@Data
public class HEADCardSummaryDto {
    private String id;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
    private boolean isDefault;
}

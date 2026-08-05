package com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADDefaultPayoutMethodDto {
    private String id;
    private String type;          // BANK_ACCOUNT / CARD
    private String brandOrBank;   // Visa / BBVA / Santander
    private String last4;
    private Boolean isDefault;
}

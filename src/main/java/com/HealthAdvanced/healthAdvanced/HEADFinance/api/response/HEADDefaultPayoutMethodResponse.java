package com.HealthAdvanced.healthAdvanced.HEADFinance.api.response;

import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADDefaultPayoutMethodDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HEADDefaultPayoutMethodResponse {
    private boolean linked;              // tiene connected account
    private boolean hasPayoutMethod;     // tiene cuenta externa disponible
    private HEADDefaultPayoutMethodDto payoutMethod;
}
package com.HealthAdvanced.healthAdvanced.HEADCommons.proposValues.values;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "head.values")
public class HEADValuesProperties {

    private Prescription prescription = new Prescription();

    @Data
    public static class Prescription {
        private int validDays = 30;
    }
}

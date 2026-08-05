package com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.propertiesModel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "telnyx")
public class TelnyxProperties {
    private String baseUrl;
    private String apiKey;
    private String verifyProfileId;
}

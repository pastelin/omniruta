package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "head.webrtc.turn")
public class HEADTurnProperties {

    private String host;
    private int port;
    private int tlsPort;
    private long ttlSeconds;
    private String secret;
}

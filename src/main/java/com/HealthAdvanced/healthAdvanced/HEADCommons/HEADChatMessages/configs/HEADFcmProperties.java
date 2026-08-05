package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "fcm")
public record HEADFcmProperties(
        String projectId,
        Resource serviceAccount
) {}
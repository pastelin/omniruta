package com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request;

public record HEADResetPassword(
        String channel,
        String identifier,
        String newPassword
) {
}

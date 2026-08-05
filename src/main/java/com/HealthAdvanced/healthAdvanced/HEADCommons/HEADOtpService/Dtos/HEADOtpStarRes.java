package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos;

public record HEADOtpStarRes(
        String txId, long expiresAt, int resendAfter, String devCode
) { }

package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos;

public record HEADVerifyRequest(String txId, String code, Boolean isVerifiedOtp, Boolean isRecoveryPassword) { }
package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.request;

public record HEADUpsertProfileRequest(
        String nombre,
        String apellidoPaterno,
        String numberPhone,
        Long sexUserId,
        String bloodType,
        Integer weightKg,
        Integer heightCm,
        String emergencyContactName,
        String emergencyPhone,
        String emergencyRelationship
){}

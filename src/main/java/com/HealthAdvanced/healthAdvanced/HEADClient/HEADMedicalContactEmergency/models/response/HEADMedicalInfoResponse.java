package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response;

import java.util.List;

public record HEADMedicalInfoResponse(User client, MedicalInfo medicalInfo, EmergencyContact emergencyContact, List<GendersList> gendersList) {

    public record User(
            String name,
            String lastName,
            String email,
            String phone,
            String profileImage,
            String dateOfBirth,
            String gender
    ) {}

    public record MedicalInfo(String bloodType, Integer weightKg, Integer heightCm) {}
    public record EmergencyContact(String fullName, String phone, String relationship) {}
    public record GendersList(String description, Long genderId) {}
}
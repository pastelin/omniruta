package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

public record HEADUpdateMyProfileRequest(
        Long sexUserId,
        String numberPhone,
        String location,
        Integer experienceYears,
        String bio
) {}

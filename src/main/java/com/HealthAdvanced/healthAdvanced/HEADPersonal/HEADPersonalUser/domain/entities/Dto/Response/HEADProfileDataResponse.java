package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

public record HEADProfileDataResponse(
        String name,
        String specialty,
        String email,
        String phone,
        String location,
        String experience,
        String photoUrl,
        String bio,
        Long gender
) {}

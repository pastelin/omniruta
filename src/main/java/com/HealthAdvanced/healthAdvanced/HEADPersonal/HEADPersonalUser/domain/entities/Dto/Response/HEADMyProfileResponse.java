package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response.HEADMedicalInfoResponse;

import java.util.List;

public record HEADMyProfileResponse(
        HEADProfileDataResponse profileData,
        HEADProfileStatsResponse stats,
        List<HEADCertificationResponse> certifications,
        List<HEADMedicalInfoResponse.GendersList> gendersList
) {}
package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADGetUserProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.request.HEADUpsertProfileRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.models.response.HEADMedicalInfoResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.service.HEADAccountInfoService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class HEADMeProfileController {

    private final HEADAccountInfoService service;

    @GetMapping("/profile")
    public HEADApiResponse<HEADMedicalInfoResponse> profile() {
        return HEADApiResponse.ok(service.getProfile());
    }

    @PutMapping("/updateProfile")
    public HEADApiResponse<HEADMedicalInfoResponse> upsertProfile(
            @Valid @RequestBody HEADUpsertProfileRequest request
    ) {
        return HEADApiResponse.ok(service.upsertProfile(request));
    }
}

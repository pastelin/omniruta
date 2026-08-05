package com.HealthAdvanced.healthAdvanced.HEADAdmin.controller;


import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts.HEADAdminService;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminLoginRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admins/auth")
@RequiredArgsConstructor
public class HEADAdminAuthController {
    private final HEADAdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<HEADApiResponse<HEADAdminLoginResponse>> login(
            @Valid @RequestBody HEADAdminLoginRequest request
    ) {
        HEADAdminLoginResponse response = adminService.login(request);
        return ResponseEntity.ok(
                HEADApiResponse.ok(response,"Login correcto")
        );
    }
}

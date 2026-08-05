package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.controllers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.dto.response.HEADTurnCredentialsResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.service.HEADTurnCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/webrtc/staff")
public class HEADTurnStaffController {

    private final HEADTurnCredentialService turnCredentialService;

    @GetMapping("/turn-credentials")
    public ResponseEntity<HEADApiResponse<HEADTurnCredentialsResponse>> getTurnCredentials() {
        return ResponseEntity.ok(HEADApiResponse.ok(turnCredentialService.generateForSubject()));
    }
}
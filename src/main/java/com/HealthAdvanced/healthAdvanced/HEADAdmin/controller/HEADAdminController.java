package com.HealthAdvanced.healthAdvanced.HEADAdmin.controller;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts.HEADAdminService;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminCreateRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminLoginRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class HEADAdminController {

    private final HEADAdminService adminService;

    @PostMapping("/users")
    public ResponseEntity<HEADApiResponse<HEADAdminResponse>> createAdmin(
            @Valid @RequestBody HEADAdminCreateRequest request
    ) {
        HEADAdminResponse response = adminService.createAdmin(request);
        return ResponseEntity.ok(
                HEADApiResponse.ok(response,"Admin creado correctamente")
        );
    }

    @GetMapping("/users")
    public ResponseEntity<HEADApiResponse<List<HEADAdminResponse>>> findAll() {
        List<HEADAdminResponse> response = adminService.findAll();
        return ResponseEntity.ok(
                HEADApiResponse.ok(response,"Admins obtenidos correctamente")
        );
    }
}

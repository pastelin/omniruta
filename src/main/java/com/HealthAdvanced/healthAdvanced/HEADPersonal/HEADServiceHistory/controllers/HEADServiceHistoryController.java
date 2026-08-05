package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.controllers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.dto.response.HEADServiceHistoryResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.services.HEADGetServiceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff/service-history")
public class HEADServiceHistoryController {

    private final HEADGetServiceHistoryService getServiceHistoryService;

    @GetMapping("/services")
    public ResponseEntity<HEADApiResponse<HEADServiceHistoryResponse>> getServiceHistory() {
        return ResponseEntity.ok(
                HEADApiResponse.ok(getServiceHistoryService.execute())
        );
    }
}
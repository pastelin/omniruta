package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.controller;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.request.HEADUpdateDoseRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response.HEADMedicationsDashboardResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.dto.response.HEADMedicationsTodayResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service.HEADMedicationsDashboardService;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.service.HEADMedicationsTodayService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/medications")
@RequiredArgsConstructor
public class HEADMeMedicationsController {

    private final HEADMedicationsTodayService service;
    private final HEADMedicationsDashboardService serviceMedDashboard;

    @GetMapping("/dashboard")
    public HEADApiResponse<HEADMedicationsDashboardResponse> dashboard() {
        return HEADApiResponse.ok(serviceMedDashboard.dashboard());
    }


    @GetMapping("/today")
    public HEADApiResponse<HEADMedicationsTodayResponse> today() {
        return HEADApiResponse.ok(service.today());
    }

    @PostMapping("/doses/{doseId}")
    public HEADApiResponse<Void> updateDose(@PathVariable long doseId, @RequestBody HEADUpdateDoseRequest req) {
        service.updateDoseStatus(doseId, req.status());
        return HEADApiResponse.ok(null, "Actualizado");
    }
}

package com.HealthAdvanced.healthAdvanced.HEADPrescription.application;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionDataResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADPrescriptionsResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.service.HEADMyPrescriptionsService;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.service.HEADPrescriptionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/prescription")
@RequiredArgsConstructor
public class HEADMePrescriptionsController {

    private final HEADMyPrescriptionsService listService;
    private final HEADPrescriptionDetailService detailService;

    @GetMapping("/list")
    public HEADApiResponse<HEADPrescriptionsResponse> list() {
        return HEADApiResponse.ok(listService.list());
    }

    @GetMapping("/detail/{id}")
    public HEADApiResponse<HEADPrescriptionDataResponse> detail(@PathVariable Long id) {
        return HEADApiResponse.ok(detailService.getById(id));
    }
}

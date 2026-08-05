package com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.controller;

import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.api.request.HEADCreateConsultingLeadRequest;
import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.api.response.HEADCreateConsultingLeadResponse;
import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.entity.HEADConsultingLead;
import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.service.HEADConsultingLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/consulting-leads")
@RequiredArgsConstructor
public class HEADConsultingLeadController {

    private final HEADConsultingLeadService service;

    @PostMapping
    public ResponseEntity<HEADCreateConsultingLeadResponse> create(
            @Valid @RequestBody HEADCreateConsultingLeadRequest request
    ) {
        HEADConsultingLead lead = service.createLead(request);

        return ResponseEntity.ok(
                new HEADCreateConsultingLeadResponse(
                        lead.getId(),
                        "Solicitud enviada correctamente"
                )
        );
    }
}
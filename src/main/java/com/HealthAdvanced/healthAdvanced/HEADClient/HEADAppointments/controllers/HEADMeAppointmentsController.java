package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.service.HEADAccountHomeService;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.models.response.HEADAppointmentsResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAppointments.services.HEADAppointmentService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class HEADMeAppointmentsController {

    private final HEADAppointmentService service;

    @GetMapping("/appointments")
    public HEADApiResponse<HEADPageResponse<HEADAppointmentsResponse.AppointmentItem>> appointments(
            @RequestParam(defaultValue = "UPCOMING") String tab,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return HEADApiResponse.ok(service.getAppointments(tab, page, size));
    }
}
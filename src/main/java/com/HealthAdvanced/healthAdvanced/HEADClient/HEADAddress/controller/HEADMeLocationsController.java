package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.controller;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.model.response.HEADLocationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.services.HEADLocationsService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class HEADMeLocationsController {

    private final HEADLocationsService service;

    @GetMapping("/locations")
    public HEADApiResponse<HEADLocationsResponse> locations() {
        return HEADApiResponse.ok(service.getLocations());
    }
}
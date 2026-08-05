package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response.HEADServiceHistoryFiltersResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response.HEADServiceHistoryGenericResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.services.HEADServiceHistoryFiltersService;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.services.HEADServiceHistoryService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class HEADMeServiceHistoryController {

    private final HEADServiceHistoryService service;
    private final HEADServiceHistoryFiltersService filterService;

    @GetMapping("/service-history")
    public HEADApiResponse<HEADServiceHistoryGenericResponse> history(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) Long occupationProfileId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return HEADApiResponse.ok(service.getHistory(q, status, occupationProfileId, page, size));
    }


    @GetMapping("/service-history/filters")
    public HEADApiResponse<HEADServiceHistoryFiltersResponse> filters() {
        return HEADApiResponse.ok(filterService.getFilters());
    }
}
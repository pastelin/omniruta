package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe.HEADStaffStripeUiStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff/stripe")
public class HEADStaffStripeUiStatusController {

    private final HEADStaffStripeUiStatusService staffStripeUiStatusService;

    @GetMapping("/ui-status")
    public ResponseEntity<HEADApiResponse<HEADStripeAccountUiResponse>> getUiStatus() {
        return ResponseEntity.ok(
                HEADApiResponse.ok(
                        staffStripeUiStatusService.getCurrentUiStatus()
                )
        );
    }
}
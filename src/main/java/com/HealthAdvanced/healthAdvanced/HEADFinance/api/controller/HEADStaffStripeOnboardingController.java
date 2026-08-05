package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountStatusResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeOnboardingLinkResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe.HEADStaffStripeOnboardingService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff/stripe")
public class HEADStaffStripeOnboardingController {

    private final HEADStaffStripeOnboardingService staffStripeOnboardingService;

    @PostMapping("/onboarding-link")
    public ResponseEntity<HEADApiResponse<HEADStripeOnboardingLinkResponse>> createOnboardingLink()
            throws StripeException {
        return ResponseEntity.ok(
                HEADApiResponse.ok(
                        staffStripeOnboardingService.createOrRefreshOnboardingLink()
                )
        );
    }

    @GetMapping("/account-status")
    public ResponseEntity<HEADApiResponse<HEADStripeAccountStatusResponse>> getAccountStatus()
            throws StripeException {
        return ResponseEntity.ok(
                HEADApiResponse.ok(
                        staffStripeOnboardingService.getCurrentStaffStripeStatus()
                )
        );
    }
}
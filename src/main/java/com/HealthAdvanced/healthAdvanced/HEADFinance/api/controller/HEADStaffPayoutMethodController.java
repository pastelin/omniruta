package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADDefaultPayoutMethodResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout.HEADStaffPayoutMethodStripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finance")
public class HEADStaffPayoutMethodController {

    private final HEADStaffPayoutMethodStripeService staffPayoutMethodStripeService;

    @GetMapping("/staff/payout-method/default")
    public ResponseEntity<HEADApiResponse<HEADDefaultPayoutMethodResponse>> getDefaultPayoutMethodForCurrentStaff()
            throws StripeException {
        return ResponseEntity.ok(
                HEADApiResponse.ok(
                        staffPayoutMethodStripeService.getDefaultPayoutMethodForCurrentStaff()
                )
        );
    }
}

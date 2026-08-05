package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/finance")
public class HEADFinanceAdminController {

    private final HEADApproveStaffPayoutService approveStaffPayoutService;
    private final HEADExecuteStaffPayoutService executeStaffPayoutService;
    private final HEADFailStaffPayoutService failStaffPayoutService;
    private final HEADRetryStaffPayoutService retryStaffPayoutService;
    private final HEADFailAndReleaseStaffPayoutService failAndReleaseStaffPayoutService;

    @PostMapping("/admin/payouts/{payoutId}/approve")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> approvePayout(
            @PathVariable Long payoutId
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                approveStaffPayoutService.execute(payoutId)
        ));
    }

    @PostMapping("/admin/payouts/{payoutId}/execute-payment")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> executePayment(
            @PathVariable Long payoutId
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(executeStaffPayoutService.execute(payoutId))
        );
    }

    @PostMapping("/admin/payouts/{payoutId}/fail")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> failPayout(
            @PathVariable Long payoutId,
            @RequestParam String note
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(failStaffPayoutService.execute(payoutId, note))
        );
    }

    @PostMapping("/admin/payouts/{payoutId}/retry")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> retryPayout(
            @PathVariable Long payoutId
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(retryStaffPayoutService.execute(payoutId))
        );
    }

    @PostMapping("/admin/payouts/{payoutId}/fail-and-release")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> failAndReleasePayout(
            @PathVariable Long payoutId,
            @RequestParam String note
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(failAndReleaseStaffPayoutService.execute(payoutId, note))
        );
    }
}

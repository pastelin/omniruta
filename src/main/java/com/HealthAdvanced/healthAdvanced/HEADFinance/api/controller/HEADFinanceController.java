package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.request.HEADRequestPayoutRequest;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADAvailablePayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADEarningsSummaryResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADMyEarningsResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings.HEADGetEarningsSummaryService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings.HEADGetMyEarningsScreenService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout.HEADApproveStaffPayoutService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout.HEADRequestStaffPayoutService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningsPeriod;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/finance")
public class HEADFinanceController {

    private final HEADGetEarningsSummaryService getEarningsSummaryService;
    private final HEADRequestStaffPayoutService requestStaffPayoutService;
    private final HEADGetMyEarningsScreenService getMyEarningsScreenService;

    @GetMapping("/staff/earnings")
    public ResponseEntity<HEADApiResponse<HEADEarningsSummaryResponse>> getEarnings(
            @RequestParam HEADPayoutPeriodType periodType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                getEarningsSummaryService.execute(periodType, page, size)
        ));
    }

    @PostMapping("/staff/payouts/availability")
    public ResponseEntity<HEADApiResponse<HEADAvailablePayoutResponse>> getAvailability(
            @RequestBody HEADRequestPayoutRequest request
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                requestStaffPayoutService.getAvailability(request)
        ));
    }

    @PostMapping("/staff/payouts/request")
    public ResponseEntity<HEADApiResponse<HEADStaffPayoutResponse>> requestPayout(
            @RequestBody HEADRequestPayoutRequest request
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                requestStaffPayoutService.requestPayout(request)
        ));
    }

    @GetMapping("/staff/my-earnings")
    public ResponseEntity<HEADApiResponse<HEADMyEarningsResponse>> getMyEarnings(
            @RequestParam(defaultValue = "WEEK") HEADEarningsPeriod period
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                getMyEarningsScreenService.execute(period)
        ));
    }
}
package com.HealthAdvanced.healthAdvanced.HEADSchedule.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.request.HEADStaffProposeScheduleRequest;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADMyScheduleResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduleDetailResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response.HEADScheduleSlotsResponse;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.service.HEADGetMyScheduleService;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.service.HEADGetScheduleDetailService;
import com.HealthAdvanced.healthAdvanced.HEADSchedule.service.HEADScheduleSlotService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class HEADScheduleController {

    private final HEADScheduleSlotService slotService;
    private final HEADGetMyScheduleService getMyScheduleService;
    private final HEADGetScheduleDetailService getScheduleDetailService;

    @PostMapping("/staff/slots")
    public ResponseEntity<HEADScheduleSlotsResponse> buildSlots(
            @RequestBody HEADStaffProposeScheduleRequest req
    ) {
        return ResponseEntity.ok(slotService.buildSlotsForStaffDay(req));
    }

    @GetMapping
    public ResponseEntity<HEADApiResponse<HEADMyScheduleResponse>> getMySchedule() {
        return ResponseEntity.ok(
                HEADApiResponse.ok(getMyScheduleService.execute())
        );
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<HEADApiResponse<HEADScheduleDetailResponse>> getScheduleDetail(
            @PathVariable Long jobId
    ) {
        return ResponseEntity.ok(
                HEADApiResponse.ok(getScheduleDetailService.execute(jobId))
        );
    }
}
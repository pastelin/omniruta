package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADStaffStateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADStaffSnapshot;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.entity.request.HEADAvailabilityRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/staff/jobs")
@RequiredArgsConstructor
public class HEADJobController {

    private final HEADJobService svc;

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id) {
        var job = svc.accept(id);
        return ResponseEntity.ok(Map.of("status","OK","state", job.getState()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
                                    @RequestBody(required=false) Map<String,String> body) {
        var job = svc.reject(id, body != null ? body.get("note") : null);
        return ResponseEntity.ok(Map.of("status","OK","state", job.getState()));
    }


    @PostMapping("/{id}/arrive")
    public ResponseEntity<?> arrive(@PathVariable Long id) {
        var job = svc.transition(id, HEADJobState.ARRIVED);
        return ResponseEntity.ok(Map.of("status","OK","state", job.getState()));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        var job = svc.transition(id, HEADJobState.STARTED);
        return ResponseEntity.ok(Map.of("status","OK","state", job.getState()));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id) {
        var job = svc.transition(id, HEADJobState.COMPLETED);
        return ResponseEntity.ok(Map.of("status","OK","state", job.getState()));
    }

    @PostMapping("/availability")
    public HEADStaffStateDto setAvailability(@RequestBody HEADAvailabilityRequest req) {
        return svc.setAvailability(req);
    }

    @GetMapping("/state")
    public HEADStaffSnapshot getState() {
        return svc.getState();
    }
}



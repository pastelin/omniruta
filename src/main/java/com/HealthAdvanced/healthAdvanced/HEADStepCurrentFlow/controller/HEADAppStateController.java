package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.controller;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.service.HEADStepCurrentClientInterface;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADNavigations.builder.HEADAppStateBuilder;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADJobStateChangedDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices.HEADStepCurrentPersonalInterface;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADAppStateDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADCurrentStepsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class HEADAppStateController {

    private final HEADCurrentStepsService currentStepsService;

    /** El front llama esto al abrir/reabrir la app */
    @GetMapping("/state/client")
    public ResponseEntity<HEADApiResponse<HEADJobStateChangedDto>> stateClient() {
       return ResponseEntity.ok(HEADApiResponse.ok(currentStepsService.currentStateClient()));
    }

    @GetMapping("/state/staff")
    public ResponseEntity<HEADApiResponse<HEADJobStateChangedDto>> stateStaff() {
        return ResponseEntity.ok(HEADApiResponse.ok(currentStepsService.currentStateStaff()));
    }
}

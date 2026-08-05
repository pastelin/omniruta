package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.controllers;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service.HEADDeleteClientAccountService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class HEADClientAuthController {
    private final HEADDeleteClientAccountService deleteAccountService;

    @DeleteMapping("/me")
    public ResponseEntity<HEADApiResponse<Boolean>> deleteMyAccount() {
        deleteAccountService.deleteClientAccount();
        return ResponseEntity.ok(HEADApiResponse.ok(true));
    }
}

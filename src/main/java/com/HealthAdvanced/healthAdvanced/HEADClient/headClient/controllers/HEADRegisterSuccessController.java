package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service.HEADClientService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clientSuccess")
@RequiredArgsConstructor
public class HEADRegisterSuccessController {

    private final HEADClientService clientService;

    @GetMapping("/registerFinal")
    public ResponseEntity<?> successRegister() {
        return clientService.successRegister();
    }
}
package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.entity.response.HEADAccountHomeResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADAccount.service.HEADAccountHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class HEADMeAccountHomeController {

    private final HEADAccountHomeService service;

    @GetMapping("/account-home")
    public HEADAccountHomeResponse home() {
        return service.getHome();
    }
}
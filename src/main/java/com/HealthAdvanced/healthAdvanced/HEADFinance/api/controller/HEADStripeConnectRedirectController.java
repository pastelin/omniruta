package com.HealthAdvanced.healthAdvanced.HEADFinance.api.controller;

import com.HealthAdvanced.healthAdvanced.HEADFinance.api.dto.HEADStripeProperties;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe.HEADStripeConnectRedirectService;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/stripe/connect")
public class HEADStripeConnectRedirectController {

    private final HEADStripeConnectRedirectService stripeConnectRedirectService;
    private final HEADStripeProperties stripeProperties;

    @GetMapping("/refresh")
    public void refresh(
            @RequestParam("account") String connectedAccountId,
            HttpServletResponse response
    ) throws Exception {
        String newStripeUrl = stripeConnectRedirectService.handleRefreshAndReturnUrl(connectedAccountId);
        response.sendRedirect(newStripeUrl);
    }

    @GetMapping("/return")
    public void handleReturn(
            @RequestParam("account") String connectedAccountId,
            HttpServletResponse response
    ) throws Exception {

        HEADStaffToStripeAccount rel = stripeConnectRedirectService.handleReturnAndSync(connectedAccountId);

        String redirectUrl = UriComponentsBuilder
                .fromHttpUrl(stripeProperties.connect().afterOnboardingRedirectUrl())
                .queryParam("account", rel.getConnectedAccountId())
                .queryParam("detailsSubmitted", rel.getDetailsSubmitted())
                .queryParam("payoutsEnabled", rel.getPayoutsEnabled())
                .queryParam("chargesEnabled", rel.getChargesEnabled())
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
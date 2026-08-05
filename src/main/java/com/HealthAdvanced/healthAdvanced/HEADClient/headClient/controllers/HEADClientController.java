package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.controllers;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADClientRegisterRequestDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request.HEADGoogleAuthRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service.HEADClientService;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.service.HEADClientWithGoogleService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADOtpResendRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADOtpService.Dtos.HEADVerifyRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEHOCodeSecurity.Model.HEADResponse.HEADCodeSecurityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientRegister/v1/clients")
@RequiredArgsConstructor
public class HEADClientController {

    private final HEADClientService clientService;
    private final HEADClientWithGoogleService googleService;

    @PostMapping("/saveClient")
    public ResponseEntity<HEADClientRegisterResponseDto> register(@RequestBody HEADClientRegisterRequestDto body) {
        return clientService.registerClient(body);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> otpVerify(@RequestBody HEADVerifyRequest body) {
        return ResponseEntity.ok(clientService.otpVerifyClient(body));
    }

    @PostMapping("/otp/start")
    public ResponseEntity<?> otpStart(@RequestBody HEADOtpRequest body) {
       return clientService.otpStartClient(body);
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<?> otpResend(@RequestBody HEADOtpResendRequest request) {
        return clientService.resendCode(request);
    }

    @PostMapping("/auth/google")
    public ResponseEntity<HEADApiResponse<?>> loginWithGoogle(@RequestBody HEADGoogleAuthRequest request) {
        return ResponseEntity.ok(HEADApiResponse.ok(googleService.registerWithGoogle(request)));
    }
}


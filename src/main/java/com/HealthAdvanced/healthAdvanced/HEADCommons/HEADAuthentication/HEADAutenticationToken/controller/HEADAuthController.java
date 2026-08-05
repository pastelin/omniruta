package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.controller;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.request.HEADLoginRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.request.HEADLogoutRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.request.HEADRefreshRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDtos.response.HEADLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class HEADAuthController {

    private final HEADAuthService authService;

    @GetMapping("/login")
    public ResponseEntity<HEADLoginResponse> login() {
        // Ojo: valida credenciales/OTP antes (no se muestra aquí)
        return ResponseEntity.ok(
                authService.login(null)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<HEADLoginResponse> refresh(@RequestBody HEADRefreshRequest req) {
        return ResponseEntity.ok(
                authService.refresh(req.getRefreshToken())
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok(true);
    }
}

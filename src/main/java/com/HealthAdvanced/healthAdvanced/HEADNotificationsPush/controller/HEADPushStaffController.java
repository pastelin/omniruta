package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants.HEADHeadersConstants;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.request.HEADRegisterFcmTokenRequest;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADPushPlatform;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service.HEADPushTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push/staff")
@RequiredArgsConstructor
public class HEADPushStaffController {

    private final HEADPushTokenService tokenService;
    private final HEADJwtGenerator jwtGenerator;
    private final HttpServletRequest request;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody HEADRegisterFcmTokenRequest req
    ) {
        String userUuid = getUserUuidFromSecurityContext();
        HEADPushPlatform platform = mapPlatform(request.getHeader(HEADHeadersConstants.DEVICE_ID));

        tokenService.registerToken(userUuid, platform, req.fcmToken());
        return ResponseEntity.ok().build();
    }

    private String getUserUuidFromSecurityContext() {
        return jwtGenerator.getUserNamePersonalUser();
    }

    private HEADPushPlatform mapPlatform(String platformHeader) {
        if (platformHeader == null) {
            return HEADPushPlatform.ANDROID;
        }

        return switch (platformHeader.toUpperCase()) {
            case "ANDROID" -> HEADPushPlatform.ANDROID;
            case "IOS"     -> HEADPushPlatform.IOS;
            case "WEB"     -> HEADPushPlatform.WEB;
            default        -> HEADPushPlatform.ANDROID;
        };
    }
}

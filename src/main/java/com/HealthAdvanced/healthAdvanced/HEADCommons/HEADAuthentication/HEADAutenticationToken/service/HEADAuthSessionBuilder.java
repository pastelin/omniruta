package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADClientRegisterResponseDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.enums.HEADTypeUser;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.HEADServiceAuthentications;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Services.HEADAppNavigatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class HEADAuthSessionBuilder {

    private final HEADJwtGenerator jwt;
    private final HEADServiceAuthentications authLoader;
    private final HEADAppNavigatorService navigator;

    /** Construye token + estado de navegación para un uuidUser dado */
    public HEADJwtUsersResponse buildUserJwtAndState(String uuidUser, Long userId, String email, String nombre,
                                                     String firstName, String lastName, Boolean isAccepted, String numberPhone) {
        var userDetails = authLoader.loadUserOrClientByUsername(uuidUser);
        var token = jwt.generateToken(userDetails, HEADTypeUser.USERS);
        var appState = navigator.resolveStateForUuid(uuidUser);

        var out = new HEADJwtUsersResponse();
        out.setUidUser(uuidUser);
        out.setIdUser(userId);
        out.setEmail(email);
        out.setName(nombre);
        out.setFirstName(firstName);
        out.setLastName(lastName);
        out.setIsAccepted(isAccepted);
        out.setNumberPhone(numberPhone);
        out.setAccessToken(token.tokenAccess());
        out.setExpiresAt(token.expiresAt());
        out.setRefreshToken(randomRefresh());
        out.setStepCurrent(appState.stepStatus()); // por compatibilidad
        // Si quieres, expón también el appState completo en otro campo del response DTO
        return out;
    }

    /** Helper para respuestas de registro de cliente que esperan tu DTO original */
    public void attachTokenAndState(HEADClientRegisterResponseDto target, String uuidUser) {
        var userDetails = authLoader.loadUserOrClientByUsername(uuidUser);
        var token = jwt.generateToken(userDetails, HEADTypeUser.USERS);
        var appState = navigator.resolveStateForUuid(uuidUser);
        target.setTokenSuccess(token.tokenAccess());
        target.setExpiresAt(token.expiresAt());
        target.setStepCurrent(appState.stepStatus());
        target.setRefreshToken(randomRefresh());
    }

    private static String randomRefresh() {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}

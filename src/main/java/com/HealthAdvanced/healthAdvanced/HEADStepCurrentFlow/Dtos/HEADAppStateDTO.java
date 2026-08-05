package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos;

import java.util.Map;

public record HEADAppStateDTO(
        String uuidUser,         // subject del JWT
        Long userId,              // id en BD
        String flow,              // CLIENT | STAFF
        String role,              // REGISTER_* | ACCESS_*

        boolean registrationDone, // todos los pasos required completos
        HEADStatusResponseDTO stepStatus, // tus DTOs actuales

        boolean hasActiveService, // si aplica
        String currentServiceId,
        String currentServiceStatus,
        String currentServiceScreen,

        String goToScreen,        // ej. "CLIENT.HOME" | "CLIENT.REGISTER.PROFILE_PASS"
        Map<String,Object> screenParams
) { }


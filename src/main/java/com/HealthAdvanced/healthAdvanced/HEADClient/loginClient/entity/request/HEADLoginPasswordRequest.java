package com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

public record HEADLoginPasswordRequest(
        String channel,     // "EMAIL" | "PHONE"
        String identifier,  // correo o teléfono
        String password    // password del usuario
) {}

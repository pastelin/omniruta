package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity;

import java.time.Instant;

// Define este record en un paquete adecuado (ej: HEADClient/entitiesDto)

public record HEADClientStateDto(
        boolean isAppActive, // Está la aplicación en primer plano o en sesión
        Double lat,
        Double lng,
        Long currentJobId, // El Job ID que el cliente tiene activo o está buscando
        long updatedAt    // Timestamp de la última actualización
) {}
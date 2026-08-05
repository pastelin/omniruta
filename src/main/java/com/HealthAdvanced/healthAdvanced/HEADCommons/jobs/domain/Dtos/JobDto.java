package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import java.math.BigDecimal;
import java.time.Instant;

// Crea un DTO simple (o usa tus DTOs reales)
public record JobDto(
        Long id, String state, Double lat, Double lng,
        BigDecimal amount, String currency,
        Instant assignedAt, Instant offerExpiresAt, Instant scheduledAt,
        Integer distanceMeters,
        Integer etaSeconds
        ) {}

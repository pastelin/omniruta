package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import lombok.Builder;

import java.util.List;

@Builder
public record HEADServiceCardDto(
        Integer id,
        String  title,
        String  subtitle,
        String  iconUrl,
        Integer sortOrder,
        boolean availableNow,
        Integer providers,
        // 👇 nuevo
        boolean hasOffer,          // true = muestra “Oferta”
        String  offerLabel         // ej. "Oferta" | "10%" | "Nueva"
) { }
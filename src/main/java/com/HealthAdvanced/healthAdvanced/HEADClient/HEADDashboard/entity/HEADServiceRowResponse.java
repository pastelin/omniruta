package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record HEADServiceRowResponse(
        Long id,                 // (tu id actual)
        Integer occupationId,    // <-- NUEVO (idOccupation)
        String  title,
        Integer providers,
        Boolean availableNow,
        Boolean hasOffer,
        String  offerLabel,
        String  iconUrl,
        String  priceLabel,      // <-- NUEVO
        String  etaLabel,        // <-- NUEVO
        Integer offerPercent,
        LocalDateTime offerEndsAt
) { }


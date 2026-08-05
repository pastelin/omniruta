package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.model.response;

import java.time.Instant;
import java.util.List;
public record HEADLocationsResponse(List<LocationItem> items) {

    public record LocationItem(
            String id,
            String addressLine1,
            boolean isPrimary,   // más usada
            boolean isRecent,    // más reciente
            int timesUsed,
            Instant lastUsedAt
    ) {}
}

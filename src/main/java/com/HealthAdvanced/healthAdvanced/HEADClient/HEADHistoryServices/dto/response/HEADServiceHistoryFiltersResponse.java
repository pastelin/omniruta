package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response;

import java.util.List;

public record HEADServiceHistoryFiltersResponse(
        List<ServiceType> serviceTypes,
        List<StatusChip> statuses
) {
    public record ServiceType(Long occupationProfileId, String name) {}
    public record StatusChip(String key, String label, long count) {}
}
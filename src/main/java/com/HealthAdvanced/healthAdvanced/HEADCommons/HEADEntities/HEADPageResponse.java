package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities;

public record HEADPageResponse<T>(
        java.util.List<T> items,
        int page,
        int size,
        long total,
        boolean hasNext,
        Integer nextPage
) {}
package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;
import org.springframework.data.domain.Page;
import java.util.function.Function;

public final class HEADPageMapper {

    private HEADPageMapper() {}

    public static <E, D> HEADPageResponse<D> map(Page<E> page, Function<E, D> mapper) {
        var items = page.getContent().stream()
                .map(mapper)
                .toList();

        boolean hasNext = page.hasNext();
        Integer nextPage = hasNext ? page.getNumber() + 1 : null;

        return new HEADPageResponse<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                hasNext,
                nextPage
        );
    }

    public static int clampSize(Integer size) {
        int def = 20;
        int max = 50;
        if (size == null) return def;
        return Math.min(Math.max(size, 1), max);
    }
}
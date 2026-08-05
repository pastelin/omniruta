package com.HealthAdvanced.healthAdvanced.HEADFinance.application.rule;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFinanceRange;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class HEADFinanceRangeResolver {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    public HEADFinanceRange resolve(HEADPayoutPeriodType periodType, Instant customFrom, Instant customTo) {
        ZonedDateTime now = ZonedDateTime.now(MX_ZONE);
        LocalDate today = now.toLocalDate();

        return switch (periodType) {
            case WEEK -> {
                LocalDate start = today.minusDays(today.getDayOfWeek().getValue() - 1L);
                yield range(start, start.plusDays(7));
            }
            case BIWEEK -> {
                int day = today.getDayOfMonth();
                LocalDate start = day <= 15 ? today.withDayOfMonth(1) : today.withDayOfMonth(16);
                LocalDate end = day <= 15 ? today.withDayOfMonth(16) : today.plusMonths(1).withDayOfMonth(1);
                yield range(start, end);
            }
            case MONTH -> {
                LocalDate start = today.withDayOfMonth(1);
                yield range(start, start.plusMonths(1));
            }
            case CUSTOM -> {
                if (customFrom == null || customTo == null || !customFrom.isBefore(customTo)) {
                    throw new HEADBadRequestException("Rango custom inválido");
                }
                yield new HEADFinanceRange(customFrom, customTo);
            }
        };
    }

    public HEADFinanceRange previousOf(HEADFinanceRange current) {
        Duration duration = Duration.between(current.from(), current.to());
        return new HEADFinanceRange(current.from().minus(duration), current.from());
    }

    private HEADFinanceRange range(LocalDate from, LocalDate to) {
        return new HEADFinanceRange(
                from.atStartOfDay(MX_ZONE).toInstant(),
                to.atStartOfDay(MX_ZONE).toInstant()
        );
    }
}
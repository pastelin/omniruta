package com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings;


import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningsPeriod;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFinanceRange;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class HEADEarningsRangeResolver {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    public HEADFinanceRange resolve(HEADEarningsPeriod period) {
        LocalDate today = LocalDate.now(MX_ZONE);

        return switch (period) {
            case WEEK -> {
                LocalDate start = today.minusDays(today.getDayOfWeek().getValue() - 1L);
                yield range(start, start.plusDays(7));
            }
            case MONTH -> {
                LocalDate start = today.withDayOfMonth(1);
                yield range(start, start.plusMonths(1));
            }
            case YEAR -> {
                LocalDate start = today.withDayOfYear(1);
                yield range(start, start.plusYears(1));
            }
        };
    }

    public HEADFinanceRange previousOf(HEADEarningsPeriod period) {
        LocalDate today = LocalDate.now(MX_ZONE);

        return switch (period) {
            case WEEK -> {
                LocalDate currentStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
                LocalDate previousStart = currentStart.minusWeeks(1);
                yield range(previousStart, currentStart);
            }
            case MONTH -> {
                LocalDate currentStart = today.withDayOfMonth(1);
                LocalDate previousStart = currentStart.minusMonths(1);
                yield range(previousStart, currentStart);
            }
            case YEAR -> {
                LocalDate currentStart = today.withDayOfYear(1);
                LocalDate previousStart = currentStart.minusYears(1);
                yield range(previousStart, currentStart);
            }
        };
    }

    private HEADFinanceRange range(LocalDate from, LocalDate to) {
        return new HEADFinanceRange(
                from.atStartOfDay(MX_ZONE).toInstant(),
                to.atStartOfDay(MX_ZONE).toInstant()
        );
    }
}

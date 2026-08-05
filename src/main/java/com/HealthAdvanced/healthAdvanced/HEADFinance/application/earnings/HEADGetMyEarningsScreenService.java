package com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADEarningTransactionItemResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADEarningsStatsResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADMyEarningsResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADPaymentMethodResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningTransactionStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADEarningsPeriod;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFinanceRange;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions.HEADMyEarningsTransactionView;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetMyEarningsScreenService {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX")).withZone(MX_ZONE);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(MX_ZONE);

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository staffRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final HEADEarningsRangeResolver earningsRangeResolver;

    public HEADMyEarningsResponse execute(HEADEarningsPeriod selectedPeriod) {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = staffRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADFinanceRange weekRange = earningsRangeResolver.resolve(HEADEarningsPeriod.WEEK);
        HEADFinanceRange monthRange = earningsRangeResolver.resolve(HEADEarningsPeriod.MONTH);
        HEADFinanceRange yearRange = earningsRangeResolver.resolve(HEADEarningsPeriod.YEAR);

        BigDecimal weeklyEarnings = value(jobFinancialRepository.sumStaffPayoutByRange(
                staff.getIdUser(), weekRange.from(), weekRange.to()
        ));

        BigDecimal monthlyEarnings = value(jobFinancialRepository.sumStaffPayoutByRange(
                staff.getIdUser(), monthRange.from(), monthRange.to()
        ));

        BigDecimal yearlyEarnings = value(jobFinancialRepository.sumStaffPayoutByRange(
                staff.getIdUser(), yearRange.from(), yearRange.to()
        ));

        HEADFinanceRange selectedRange = earningsRangeResolver.resolve(selectedPeriod);
        HEADFinanceRange previousRange = earningsRangeResolver.previousOf(selectedPeriod);

        var currentAgg = jobFinancialRepository.aggregateStaffEarnings(
                staff.getIdUser(),
                selectedRange.from(),
                selectedRange.to()
        );

        var previousAgg = jobFinancialRepository.aggregateStaffEarnings(
                staff.getIdUser(),
                previousRange.from(),
                previousRange.to()
        );

        BigDecimal currentTotal = value(currentAgg.totalEarned());
        BigDecimal previousTotal = value(previousAgg.totalEarned());

        int growthPercentage = calculateGrowth(previousTotal, currentTotal);

        int totalServices = Optional.ofNullable(currentAgg.totalJobs()).orElse(0L).intValue();

        double averageAmount = totalServices > 0
                ? currentTotal.divide(BigDecimal.valueOf(totalServices), 2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        int hoursWorked = Optional.ofNullable(currentAgg.totalDurationSeconds())
                .map(seconds -> Math.toIntExact(seconds / 3600))
                .orElse(0);

        List<HEADEarningTransactionItemResponse> transactions = jobFinancialRepository.findMyEarningsTransactions(
                        staff.getIdUser(),
                        selectedRange.from(),
                        selectedRange.to(),
                        PageRequest.of(0, 20)
                ).stream()
                .map(this::toTransactionItem)
                .toList();

        return new HEADMyEarningsResponse(
                selectedPeriod,
                weeklyEarnings.doubleValue(),
                monthlyEarnings.doubleValue(),
                yearlyEarnings.doubleValue(),
                growthPercentage,
                new HEADEarningsStatsResponse(
                        totalServices,
                        averageAmount,
                        hoursWorked
                ),
                transactions,
                null
        );
    }

    private HEADEarningTransactionItemResponse toTransactionItem(HEADMyEarningsTransactionView row) {
        String patient = Stream.of(row.getNombre(), row.getPaterno())
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        return new HEADEarningTransactionItemResponse(
                row.getId(),
                Optional.ofNullable(row.getType()).orElse(""),
                patient,
                value(row.getAmount()).doubleValue(),
                row.getCompletedAt() != null ? DATE_FORMAT.format(row.getCompletedAt()) : "",
                row.getCompletedAt() != null ? TIME_FORMAT.format(row.getCompletedAt()) : "",
                mapTransactionStatus(row.getPayoutStatus())
        );
    }

    private HEADEarningTransactionStatus mapTransactionStatus(String payoutStatus) {
        return switch (String.valueOf(payoutStatus)) {
            case "AVAILABLE", "RESERVED", "PAID" -> HEADEarningTransactionStatus.COMPLETED;
            default -> HEADEarningTransactionStatus.PENDING;
        };
    }

    private int calculateGrowth(BigDecimal previous, BigDecimal current) {
        return Optional.of(previous)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .map(p -> current.subtract(p)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(p, 2, RoundingMode.HALF_UP))
                .map(pct -> pct.setScale(0, RoundingMode.HALF_UP).intValue())
                .orElse(current.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0);
    }

    private BigDecimal value(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
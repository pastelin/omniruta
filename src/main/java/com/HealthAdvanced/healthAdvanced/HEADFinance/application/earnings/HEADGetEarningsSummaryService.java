package com.HealthAdvanced.healthAdvanced.HEADFinance.application.earnings;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADPageMapper;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADEarningTransactionResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADEarningsSummaryResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.rule.HEADFinanceRangeResolver;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFinanceRange;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions.HEADStaffEarningTransactionView;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HEADGetEarningsSummaryService {

    private static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "MX")).withZone(MX_ZONE);
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm").withZone(MX_ZONE);

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository staffRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final HEADFinanceRangeResolver rangeResolver;

    public HEADEarningsSummaryResponse execute(HEADPayoutPeriodType periodType, Integer page, Integer size) {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = staffRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        int safePage = Optional.ofNullable(page).filter(p -> p >= 0).orElse(0);
        int safeSize = Optional.ofNullable(size).filter(s -> s > 0).map(s -> Math.min(s, 50)).orElse(20);

        HEADFinanceRange current = rangeResolver.resolve(periodType, null, null);
        HEADFinanceRange previous = rangeResolver.previousOf(current);

        var currentAgg = jobFinancialRepository.aggregateStaffEarnings(
                staff.getIdUser(),
                current.from(),
                current.to()
        );

        var previousAgg = jobFinancialRepository.aggregateStaffEarnings(
                staff.getIdUser(),
                previous.from(),
                previous.to()
        );

        BigDecimal total = jobFinancialRepository.sumAvailableForPayout(
                staff.getIdUser(),
                "MXN",
                HEADJobPayoutStatus.AVAILABLE,
                Instant.now(),
                current.from(),
                current.to()
        );

        BigDecimal totalEarned = value(currentAgg.totalEarned());
        long totalServices = Optional.ofNullable(currentAgg.totalJobs()).orElse(0L);
        long totalDurationSeconds = Optional.ofNullable(currentAgg.totalDurationSeconds()).orElse(0L);

        BigDecimal averagePerService = totalServices > 0
                ? totalEarned.divide(BigDecimal.valueOf(totalServices), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        var txPage = jobFinancialRepository.findStaffTransactions(
                staff.getIdUser(),
                current.from(),
                current.to(),
                PageRequest.of(safePage, safeSize)
        );

        var transactions = HEADPageMapper.map(txPage, this::toTransactionResponse);

        boolean canWithdraw = total != null && total.compareTo(BigDecimal.ZERO) > 0;
        return new HEADEarningsSummaryResponse(
                totalEarned.setScale(2, RoundingMode.HALF_UP),
                "MXN",
                growthLabel(value(previousAgg.totalEarned()), totalEarned),
                totalServices,
                averagePerService,
                total,
                canWithdraw,
                totalDurationSeconds / 3600,
                transactions
        );
    }

    private HEADEarningTransactionResponse toTransactionResponse(HEADStaffEarningTransactionView row) {
        String patientName = Stream.of(row.getNombre(), row.getPaterno())
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        return new HEADEarningTransactionResponse(
                row.getJobId(),
                Optional.ofNullable(row.getServiceName()).orElse(""),
                patientName,
                value(row.getAmount()).setScale(2, RoundingMode.HALF_UP),
                Optional.ofNullable(row.getPayoutStatus()).orElse(""),
                row.getCompletedAt() != null ? DATE_FORMAT.format(row.getCompletedAt()) : "",
                row.getCompletedAt() != null ? TIME_FORMAT.format(row.getCompletedAt()) : ""
        );
    }

    private String growthLabel(BigDecimal previous, BigDecimal current) {
        return Optional.of(previous)
                .filter(p -> p.compareTo(BigDecimal.ZERO) > 0)
                .map(p -> current.subtract(p)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(p, 2, RoundingMode.HALF_UP))
                .map(pct -> (pct.signum() >= 0 ? "+" : "") + pct.setScale(0, RoundingMode.HALF_UP) + "%")
                .orElse(current.compareTo(BigDecimal.ZERO) > 0 ? "Nuevo" : "0%");
    }

    private BigDecimal value(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
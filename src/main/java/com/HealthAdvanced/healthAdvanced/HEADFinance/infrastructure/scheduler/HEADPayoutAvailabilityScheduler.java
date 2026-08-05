package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.scheduler;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class HEADPayoutAvailabilityScheduler {

    private final HEADJobFinancialRepository jobFinancialRepository;

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void releaseOnHoldPayouts() {
        int updated = jobFinancialRepository.releasePayouts(
                HEADJobPayoutStatus.ON_HOLD,
                HEADJobPayoutStatus.AVAILABLE,
                Instant.now()
        );

        if (updated > 0) {
            log.info("Finance -> payouts liberados: {}", updated);
        }
    }
}
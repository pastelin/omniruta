package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.policy;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPlatformFeeRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class HEADPayoutEligibilityPolicy {

    public Instant resolveAvailableAt(Instant completedAt, HEADPlatformFeeRule rule) {
        int holdDays = rule.getHoldDays() != null ? rule.getHoldDays() : 0;
        return completedAt.plus(holdDays, ChronoUnit.DAYS);
    }

    public HEADJobPayoutStatus resolveInitialStatus(HEADPlatformFeeRule rule) {
        return (rule.getHoldDays() != null && rule.getHoldDays() > 0)
                ? HEADJobPayoutStatus.ON_HOLD
                : HEADJobPayoutStatus.AVAILABLE;
    }
}
package com.HealthAdvanced.healthAdvanced.HEADFinance.application.rule;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPlatformFeeRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADPlatformFeeRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HEADPlatformFeeRuleSelector {

    private final HEADPlatformFeeRuleRepository platformFeeRuleRepository;

    public HEADPlatformFeeRule resolve(Instant effectiveAt, Integer durationMin) {
        var matches = platformFeeRuleRepository.findMatchingRules(effectiveAt, durationMin);

        if (!matches.isEmpty()) {
            return matches.get(0);
        }

        return platformFeeRuleRepository
                .findTopByActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(effectiveAt)
                .orElseThrow(() -> new HEADBadRequestException("No existe regla activa de platform fee"));
    }
}
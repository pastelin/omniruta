package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;


import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPaymentProcessorRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface HEADPaymentProcessorRuleRepository extends JpaRepository<HEADPaymentProcessorRule, Long> {

    Optional<HEADPaymentProcessorRule> findTopByProcessorAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            HEADPaymentProcessor processor,
            Instant effectiveAt
    );
}

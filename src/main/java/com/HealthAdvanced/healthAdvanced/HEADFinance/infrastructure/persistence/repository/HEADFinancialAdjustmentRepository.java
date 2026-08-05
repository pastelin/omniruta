package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADFinancialAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HEADFinancialAdjustmentRepository extends JpaRepository<HEADFinancialAdjustment, Long> {
}
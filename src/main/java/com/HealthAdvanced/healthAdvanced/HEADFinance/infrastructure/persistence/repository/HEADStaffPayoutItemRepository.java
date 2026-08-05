package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayoutItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HEADStaffPayoutItemRepository extends JpaRepository<HEADStaffPayoutItem, Long> {
    List<HEADStaffPayoutItem> findByPayout_Id(Long payoutId);
}
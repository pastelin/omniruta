package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface HEADStaffPayoutRepository extends JpaRepository<HEADStaffPayout, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from HEADStaffPayout p
        where p.id = :payoutId
    """)
    Optional<HEADStaffPayout> findByIdForUpdate(@Param("payoutId") Long payoutId);

    Optional<HEADStaffPayout> findByExternalPayoutId(String externalPayoutId);
}
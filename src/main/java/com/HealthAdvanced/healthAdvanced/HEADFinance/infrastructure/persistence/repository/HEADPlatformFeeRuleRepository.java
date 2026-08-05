package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPlatformFeeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HEADPlatformFeeRuleRepository extends JpaRepository<HEADPlatformFeeRule, Long> {

    Optional<HEADPlatformFeeRule> findTopByActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            Instant effectiveAt
    );

    @Query("""
        select r
        from HEADPlatformFeeRule r
        where r.active = true
          and r.effectiveFrom <= :effectiveAt
          and (:durationMin is null or
               (
                 (r.minDurationMin is null or r.minDurationMin <= :durationMin)
                 and
                 (r.maxDurationMin is null or r.maxDurationMin >= :durationMin)
               )
          )
        order by
          case when r.minDurationMin is null and r.maxDurationMin is null then 1 else 0 end asc,
          r.effectiveFrom desc,
          r.minDurationMin desc
    """)
    List<HEADPlatformFeeRule> findMatchingRules(
            @Param("effectiveAt") Instant effectiveAt,
            @Param("durationMin") Integer durationMin
    );

}
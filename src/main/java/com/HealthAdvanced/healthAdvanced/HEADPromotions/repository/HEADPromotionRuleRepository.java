package com.HealthAdvanced.healthAdvanced.HEADPromotions.repository;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface HEADPromotionRuleRepository extends JpaRepository<HEADPromotionRule, Long> {
    @Query("""
        select r
        from HEADPromotionRule r
        where r.promotion.id = :promotionId
          and r.enabled = true
        order by r.priority desc, r.id asc
    """)
    List<HEADPromotionRule> findEnabledByPromotion(@Param("promotionId") long promotionId);

    @Query("""
        select r
        from HEADPromotionRule r
        where r.promotion.id in :promotionIds
          and r.enabled = true
        order by r.promotion.id asc, r.priority desc, r.id asc
    """)
    List<HEADPromotionRule> findEnabledByPromotions(@Param("promotionIds") List<Long> promotionIds);

    @Query("""
      select r
      from HEADPromotionRule r
      where r.promotion.id in :promoIds
        and r.enabled = true
    """)
    List<HEADPromotionRule> findEnabledRulesForPromotions(@Param("promoIds") Collection<Long> promoIds);
}
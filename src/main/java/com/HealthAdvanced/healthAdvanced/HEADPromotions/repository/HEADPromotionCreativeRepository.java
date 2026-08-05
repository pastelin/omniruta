package com.HealthAdvanced.healthAdvanced.HEADPromotions.repository;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionCreative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HEADPromotionCreativeRepository extends JpaRepository<HEADPromotionCreative, Long> {

    @Query("""
    select c
    from HEADPromotionCreative c
    where c.enabled = true
      and (c.startsAt is null or c.startsAt <= :now)
      and (c.endsAt   is null or c.endsAt   >= :now)
      and c.variant = :variant
    order by coalesce(c.priority, 0) desc,
             coalesce(c.sortKey, 999999) asc,
             c.id desc
""")
    List<HEADPromotionCreative> findActiveByVariant(
            @Param("variant") HEADCardVariant variant,
            @Param("now") LocalDateTime now
    );

    @Query("""
        select c
        from HEADPromotionCreative c
        where c.enabled = true
          and c.promotion.id = :promotionId
          and (c.startsAt is null or c.startsAt <= :now)
          and (c.endsAt   is null or c.endsAt   >= :now)
        order by coalesce(c.priority, 0) desc, coalesce(c.sortKey, 999999) asc, c.id desc
    """)
    List<HEADPromotionCreative> findActiveByPromotionId(
            @Param("promotionId") Long promotionId,
            @Param("now") LocalDateTime now
    );
}
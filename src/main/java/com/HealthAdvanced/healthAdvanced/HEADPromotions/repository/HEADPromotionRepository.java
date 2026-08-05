package com.HealthAdvanced.healthAdvanced.HEADPromotions.repository;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface HEADPromotionRepository extends JpaRepository<HEADPromotion, Long> {
    @Query("""
    SELECT p FROM HEADPromotion p
     WHERE p.status = :status
       AND (p.startsAt IS NULL OR p.startsAt <= :now)
       AND (p.endsAt   IS NULL OR p.endsAt   >= :now)
  """)
    List<HEADPromotion> findActive(@Param("now") LocalDateTime now,
                                   @Param("status") HEADPromotionStatus status);

    @Query("""
    SELECT p FROM HEADPromotion p
     WHERE p.status = :status
       AND p.targetType = :type
       AND p.targetId IN :ids
       AND (p.startsAt IS NULL OR p.startsAt <= :now)
       AND (p.endsAt   IS NULL OR p.endsAt   >= :now)
     ORDER BY p.priority DESC, p.id DESC
  """)
    List<HEADPromotion> findActiveForTargets(@Param("type") HEADPromotionTargetType type,
                                             @Param("ids") Collection<String> ids,
                                             @Param("now") LocalDateTime now,
                                             @Param("status") HEADPromotionStatus status);
}

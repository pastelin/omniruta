package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model.HEADStaffRatingSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

import java.util.Optional;

public interface HEADStaffRatingSummaryRepository extends JpaRepository<HEADStaffRatingSummary, Long> {

    Optional<HEADStaffRatingSummary> findByIdPersonalUser_IdUser(Long staffUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s
          from HEADStaffRatingSummary s
         where s.staffUserId = :staffUserId
    """)
    Optional<HEADStaffRatingSummary> findByIdForUpdate(@Param("staffUserId") Long staffUserId);

    @Query("""
        select count(distinct s.staffUserId)
        from HEADStaffRatingSummary s
        join s.idPersonalUser.occupationLinks link
        where link.idOccupationProfile.IdOccupationProfile = :occupationProfileId
          and s.totalReviews >= :minReviews
    """)
    long countPeersByOccupationProfile(@Param("occupationProfileId") Long occupationProfileId,
                                       @Param("minReviews") Integer minReviews);

    @Query("""
        select count(distinct s.staffUserId)
        from HEADStaffRatingSummary s
        join s.idPersonalUser.occupationLinks link
        where link.idOccupationProfile.IdOccupationProfile = :occupationProfileId
          and s.totalReviews >= :minReviews
          and (
                s.bayesianScore > :myBayesianScore
                or (s.bayesianScore = :myBayesianScore and s.totalReviews > :myTotalReviews)
                or (s.bayesianScore = :myBayesianScore and s.totalReviews = :myTotalReviews and s.staffUserId < :staffUserId)
          )
    """)
    long countBetterPeersByOccupationProfile(@Param("occupationProfileId") Long occupationProfileId,
                                             @Param("minReviews") Integer minReviews,
                                             @Param("myBayesianScore") BigDecimal myBayesianScore,
                                             @Param("myTotalReviews") Integer myTotalReviews,
                                             @Param("staffUserId") Long staffUserId);


}


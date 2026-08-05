package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import java.util.Optional;

@Repository
public interface HEADPackagesPersonalRepository extends JpaRepository<HEADPackagesPersonal, String> {

    @Query("""
        select p
        from HEADPackagesToProfiles ptp
        join ptp.idPackageAvailable p
        where ptp.idOccupationProfile.IdOccupationProfile = :profileId
          and ptp.isActive = true
          and p.active = true
        order by p.sortOrder asc, p.title asc
    """)
    List<HEADPackagesPersonal> findAllByProfile(@Param("profileId") Long profileId);

    @Query("""
        select min(po.priceFrom)
        from HEADPackagesToProfiles ptp
        join ptp.idPackageAvailable p
        join HEADPackageOption po on po.pkg.id = p.id
        where ptp.idOccupationProfile.IdOccupationProfile = :profileId
          and ptp.isActive = true
          and p.active = true
          and po.active = true
    """)
    BigDecimal findMinPriceFromByProfile(@Param("profileId") Long profileId);

    @Query("""
        select p
        from HEADPackagesPersonal p
        where p.id = :packageId
          and p.active = true
    """)
    Optional<HEADPackagesPersonal> findActiveById(@Param("packageId") String packageId);
}
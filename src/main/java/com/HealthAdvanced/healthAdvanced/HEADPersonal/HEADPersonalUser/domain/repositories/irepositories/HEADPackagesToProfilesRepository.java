package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;


import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesToProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADPackagesToProfilesRepository extends JpaRepository<HEADPackagesToProfiles, Long> {
    Optional<List<HEADPackagesToProfiles>> findByIdOccupationProfile(HEADOccupationProfile idOccupationProfile);

    Optional<List<HEADPackagesToProfiles>> findByIdPackageAvailable(HEADPackagesPersonal idPackageAvailable);


    @Query("""
SELECT DISTINCT p
  FROM HEADPackagesToProfiles ptp
  JOIN ptp.idPackageAvailable p
 WHERE ptp.isActive = true
   AND p.active = true
   AND ptp.idOccupationProfile.IdOccupationProfile IN :profileIds
 ORDER BY p.sortOrder DESC, p.title ASC
""")
    List<HEADPackagesPersonal> findActivePackagesForProfiles(@Param("profileIds") List<Long> profileIds);

    @Query("""
SELECT op.IdOccupationProfile
  FROM HEADPackagesToProfiles ptp
  JOIN ptp.idOccupationProfile op
 WHERE ptp.isActive = true
   AND ptp.idPackageAvailable.id = :packageId
""")
    List<Long> findActiveProfileIdsByPackage(@Param("packageId") String packageId);

    @Query("""
SELECT DISTINCT op.idOccupation.idOccupation
  FROM HEADPackagesToProfiles ptp
  JOIN ptp.idOccupationProfile op
 WHERE ptp.isActive = true
   AND ptp.idPackageAvailable.id = :packageSlug
""")
    List<Integer> findOccupationIdsByPackageSlug(@Param("packageSlug") String packageSlug);

}

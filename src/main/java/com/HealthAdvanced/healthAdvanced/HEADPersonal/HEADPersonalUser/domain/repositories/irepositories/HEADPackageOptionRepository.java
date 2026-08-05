package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;


import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADPackageOptionRepository extends JpaRepository<HEADPackageOption, Long> {

    List<HEADPackageOption> findByPkg_IdAndActiveTrueOrderBySortOrderAscOptionLabelAsc(String packageId);

    @Query("""
        select po
        from HEADPackageOption po
        join fetch po.pkg p
        where po.id = :optionId
          and po.active = true
    """)
    Optional<HEADPackageOption> findActiveByIdWithPackage(@Param("optionId") Long optionId);

    @Query("""
        select po
        from HEADPackageOption po
        join fetch po.pkg p
        where p.id in :packageIds
          and po.active = true
        order by p.id asc, po.sortOrder asc, po.optionLabel asc
    """)
    List<HEADPackageOption> findAllActiveByPackageIds(@Param("packageIds") List<String> packageIds);

    @Query("""
        select po
        from HEADPackageOption po
        where po.pkg.id = :packageId
          and po.active = true
        order by po.sortOrder asc, po.optionLabel asc
    """)
    List<HEADPackageOption> findAllActiveByPackageId(@Param("packageId") String packageId);

    boolean existsByPkg_IdAndActiveTrue(String packageId);
}
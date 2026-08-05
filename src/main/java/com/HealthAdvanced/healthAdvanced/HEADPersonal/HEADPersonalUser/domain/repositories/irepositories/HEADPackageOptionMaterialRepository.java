package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOptionMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HEADPackageOptionMaterialRepository extends JpaRepository<HEADPackageOptionMaterial, Long> {

    @Query("""
        select pom
        from HEADPackageOptionMaterial pom
        where pom.packageOption.id = :packageOptionId
          and pom.active = true
        order by pom.sortOrder asc, pom.materialName asc
    """)
    List<HEADPackageOptionMaterial> findAllActiveByPackageOptionId(@Param("packageOptionId") Long packageOptionId);

    boolean existsByPackageOption_IdAndActiveTrue(Long packageOptionId);
}
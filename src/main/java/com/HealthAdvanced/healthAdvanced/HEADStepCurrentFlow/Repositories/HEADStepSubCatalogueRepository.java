package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HEADStepSubCatalogueRepository extends JpaRepository<HEADStepSubCatalogue, Long> {
    List<HEADStepSubCatalogue> findByStepParent_IdCatalogueOrderByOrderNoAsc(Long parentId);
}

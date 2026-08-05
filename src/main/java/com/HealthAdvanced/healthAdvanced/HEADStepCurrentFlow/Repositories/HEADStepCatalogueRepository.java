package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Repositories;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADStepCatalogueRepository extends JpaRepository<HEADStepCurrentCatalogue, Long> {
    List<HEADStepCurrentCatalogue> findByTypeFlowOrderByOrderNoAsc(String typeFlow);
    Optional<HEADStepCurrentCatalogue> findByTypeFlowAndStepName(String typeFlow, String stepName);
}

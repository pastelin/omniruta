package com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepCurrentClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADStepCurrentClientRepository extends JpaRepository<HEADStepCurrentClient, Long> {
    @EntityGraph(attributePaths = "idStepCatalogue")
    List<HEADStepCurrentClient> findByIdClient_IdUser(Long clientId);

    Optional<HEADStepCurrentClient> findByIdClient_IdUserAndIdStepCatalogue_IdCatalogue(Long clientId, Long stepId);

    boolean existsByIdClient_IdUserAndIdStepCatalogue_IdCatalogueAndIsCompleteStepsTrue(Long clientId, Long stepId);
}

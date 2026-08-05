package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepCurrentPersonal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADStepCurrentPersonalRepository extends JpaRepository<HEADStepCurrentPersonal, Long> {
    @EntityGraph(attributePaths = "idStepCatalogue")
    List<HEADStepCurrentPersonal> findByIdPersonalUser_IdUser(Long staffId);

    Optional<HEADStepCurrentPersonal> findByIdPersonalUser_IdUserAndIdStepCatalogue_IdCatalogue(Long staffId, Long stepId);

    boolean existsByIdPersonalUser_IdUserAndIdStepCatalogue_IdCatalogueAndIsCompleteStepsTrue(Long staffId, Long stepId);
}

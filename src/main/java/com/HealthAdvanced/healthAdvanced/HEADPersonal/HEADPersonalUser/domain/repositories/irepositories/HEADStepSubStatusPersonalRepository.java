package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepSubStatusPersonal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADStepSubStatusPersonalRepository extends JpaRepository<HEADStepSubStatusPersonal, Long> {
    @EntityGraph(attributePaths = "sub")
    List<HEADStepSubStatusPersonal> findByIdPersonalUser_IdUser(Long staffId);

    Optional<HEADStepSubStatusPersonal> findByIdPersonalUser_IdUserAndSub_IdSub(Long staffId, Long subId);

    boolean existsByIdPersonalUser_IdUserAndSub_IdSubAndIsCompleteTrue(Long staffId, Long subId);
}

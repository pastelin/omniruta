package com.HealthAdvanced.healthAdvanced.HEADClient.stepCurrentClient.repository;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADStepSubStatusClient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HEADStepSubStatusClientRepository extends JpaRepository<HEADStepSubStatusClient, Long> {
    @EntityGraph(attributePaths = "sub")
    List<HEADStepSubStatusClient> findByIdClient_IdUser(Long clientId);

    Optional<HEADStepSubStatusClient> findByIdClient_IdUserAndSub_IdSub(Long clientId, Long subId);

    boolean existsByIdClient_IdUserAndSub_IdSubAndIsCompleteTrue(Long clientId, Long subId);
}

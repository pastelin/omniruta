package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientMedicalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HEADClientMedicalInfoRepository extends JpaRepository<HEADClientMedicalInfo, Long> {

    Optional<HEADClientMedicalInfo> findByClient_IdUser(Long clientId);

    boolean existsByClient_IdUser(Long clientId);

    void deleteByClient_IdUser(Long clientId);
}
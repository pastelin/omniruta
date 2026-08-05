package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.repository;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HEADClientEmergencyContactRepository extends JpaRepository<HEADClientEmergencyContact, Long> {

    Optional<HEADClientEmergencyContact> findByClient_IdUser(Long clientId);

    boolean existsByClient_IdUser(Long clientId);

    void deleteByClient_IdUser(Long clientId);
}
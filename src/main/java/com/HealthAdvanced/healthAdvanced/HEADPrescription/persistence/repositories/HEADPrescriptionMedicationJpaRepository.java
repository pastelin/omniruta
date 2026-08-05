package com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HEADPrescriptionMedicationJpaRepository extends JpaRepository<HEADPrescriptionMedication, Long> {
    List<HEADPrescriptionMedication> findByPrescription_IdOrderByLineNoAsc(Long prescriptionId);

}

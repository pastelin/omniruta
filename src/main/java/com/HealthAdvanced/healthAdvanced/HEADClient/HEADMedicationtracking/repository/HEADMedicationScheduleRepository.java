package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADPrescriptionRangeView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model.HEADMedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HEADMedicationScheduleRepository extends JpaRepository<HEADMedicationSchedule, Long> {
    Optional<HEADMedicationSchedule> findByPrescriptionMedication_Id(Long medicationId);
    List<HEADMedicationSchedule> findAllByClientUuidAndActiveTrue(String clientUuid);
    List<HEADMedicationSchedule> findAllByPrescription_Id(Long prescriptionId);


    @Query("""
  select s.prescription.id as prescriptionId,
         min(s.startDate) as startDate,
         max(s.endDate) as endDate
  from HEADMedicationSchedule s
  where s.clientUuid = :clientUuid
    and s.active = true
    and s.prescription.id in :prescriptionIds
  group by s.prescription.id
""")
    List<HEADPrescriptionRangeView> findRangesByClientAndPrescriptionIds(
            @Param("clientUuid") String clientUuid,
            @Param("prescriptionIds") Collection<Long> prescriptionIds
    );
}

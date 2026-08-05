package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.repository;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADDoseDailyAggView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADDoseDayAggView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADDoseTodayRow;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.interfaces.HEADNextDoseView;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model.HEADMedicationDose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HEADMedicationDoseRepository extends JpaRepository<HEADMedicationDose, Long> {

    List<HEADMedicationDose> findAllByClientUuidAndDoseDateOrderByDoseTimeAsc(String clientUuid, LocalDate date);

    boolean existsBySchedule_IdAndDoseDateAndDoseTime(Long scheduleId, LocalDate date, java.time.LocalTime time);

    @Query("""
        select d
        from HEADMedicationDose d
        where d.clientUuid = :clientUuid
          and d.doseDate = :date
        order by d.doseTime asc
    """)
    List<HEADMedicationDose> findToday(@Param("clientUuid") String clientUuid, @Param("date") LocalDate date);

    @Query("""
  select d.doseTime
  from HEADMedicationDose d
  where d.schedule.id = :scheduleId
    and d.doseDate = :date
""")
    List<LocalTime> findExistingDoseTimes(@Param("scheduleId") Long scheduleId,
                                          @Param("date") LocalDate date);

    Optional<HEADMedicationDose> findByIdAndClientUuid(Long id, String clientUuid);

    long countByClientUuidAndDoseDate(String clientUuid, LocalDate date);

    long countByClientUuidAndDoseDateAndStatus(String clientUuid, LocalDate date, HEADDoseStatus status);

    @Query("""
  select count(d.id)
  from HEADMedicationDose d
  where d.clientUuid = :clientUuid
    and d.schedule.prescription.id = :prescriptionId
    and d.doseDate between :from and :to
""")
    long countTotalByPrescriptionInRange(
            @Param("clientUuid") String clientUuid,
            @Param("prescriptionId") Long prescriptionId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
  select count(d.id)
  from HEADMedicationDose d
  where d.clientUuid = :clientUuid
    and d.schedule.prescription.id = :prescriptionId
    and d.status = :status
    and d.doseDate between :from and :to
""")
    long countByPrescriptionStatusInRange(
            @Param("clientUuid") String clientUuid,
            @Param("prescriptionId") Long prescriptionId,
            @Param("status") HEADDoseStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
  select d.schedule.prescription.id as prescriptionId,
         d.doseDate as doseDate,
         count(d.id) as total,
         sum(case when d.status = 'TAKEN' then 1 else 0 end) as taken
  from HEADMedicationDose d
  where d.clientUuid = :clientUuid
    and d.schedule.prescription.id in :prescriptionIds
    and d.doseDate between :from and :to
  group by d.schedule.prescription.id, d.doseDate
""")
    List<HEADDoseDayAggView> aggregateByPrescriptionAndDay(
            @Param("clientUuid") String clientUuid,
            @Param("prescriptionIds") Collection<Long> prescriptionIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
  select
    d.id as doseId,
    d.doseTime as doseTime,
    d.status as status,
    pm.id as medicationId,
    pm.name as medicationName,
    pm.dosage as medicationDosage,
    pm.medForm as medForm
  from HEADMedicationDose d
  join d.schedule s
  join s.prescriptionMedication pm
  where d.clientUuid = :clientUuid
    and d.doseDate = :date
  order by pm.id asc, d.doseTime asc
""")
    List<HEADDoseTodayRow> findTodayRows(
            @Param("clientUuid") String clientUuid,
            @Param("date") LocalDate date
    );

    @Query("""
      select
        d.id as doseId,
        d.doseTime as doseTime,
        pm.id as medicationId,
        pm.name as medicationName,
        pm.dosage as medicationDosage,
        pm.medForm as medForm
      from HEADMedicationDose d
      join d.schedule s
      join s.prescriptionMedication pm
      where d.clientUuid = :clientUuid
        and d.doseDate = :date
        and d.status = 'PENDING'
        and d.doseTime >= :nowTime
      order by d.doseTime asc
    """)
    List<HEADNextDoseView> findNextPendingToday(
            @Param("clientUuid") String clientUuid,
            @Param("date") LocalDate date,
            @Param("nowTime") LocalTime nowTime
    );

    @Query("""
      select
        d.doseDate as doseDate,
        count(d.id) as total,
        sum(case when d.status = 'TAKEN' then 1 else 0 end) as taken,
        sum(case when d.status = 'PENDING' then 1 else 0 end) as pending
      from HEADMedicationDose d
      where d.clientUuid = :clientUuid
        and d.doseDate between :from and :to
      group by d.doseDate
      order by d.doseDate desc
    """)
    List<HEADDoseDailyAggView> aggregateDaily(
            @Param("clientUuid") String clientUuid,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}

package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.enums.HEADDoseStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_med_dose",
        indexes = {
                @Index(name="idx_dose_sched_date", columnList="schedule_id,dose_date"),
                @Index(name="idx_dose_client_date", columnList="client_uuid,dose_date"),
                @Index(name="idx_dose_status", columnList="status")
        }
)
public class HEADMedicationDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="schedule_id", nullable=false)
    private HEADMedicationSchedule schedule;

    @Column(name="client_uuid", nullable=false, length=64)
    private String clientUuid;

    @Column(name="dose_date", nullable=false)
    private LocalDate doseDate;

    @Column(name="dose_time", nullable=false)
    private LocalTime doseTime;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=16)
    private HEADDoseStatus status = HEADDoseStatus.PENDING;

    @Column(name="taken_at")
    private Instant takenAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }
}
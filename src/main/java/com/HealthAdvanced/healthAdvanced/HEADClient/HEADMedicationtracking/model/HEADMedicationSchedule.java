package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicationtracking.model;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADFrequencyMode;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescription;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities.HEADPrescriptionMedication;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_med_schedule",
        indexes = {
                @Index(name="idx_sched_client_uuid", columnList="client_uuid"),
                @Index(name="idx_sched_prescription_id", columnList="prescription_id"),
                @Index(name="idx_sched_med_id", columnList="prescription_medication_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_sched_med", columnNames={"prescription_medication_id"})
        }
)
public class HEADMedicationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="client_uuid", nullable=false, length=64)
    private String clientUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="prescription_id", nullable=false)
    private HEADPrescription prescription;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="prescription_medication_id", nullable=false, unique=true)
    private HEADPrescriptionMedication prescriptionMedication;

    @Column(name="start_date", nullable=false)
    private LocalDate startDate;

    @Column(name="end_date", nullable=false)
    private LocalDate endDate;

    @Column(name="frequency_mode", length=20, nullable=false)
    @Enumerated(EnumType.STRING)
    private HEADFrequencyMode frequencyMode;

    @Column(name="times_per_day")
    private Integer timesPerDay;

    @Column(name="interval_hours")
    private Integer intervalHours;

    @Column(name="timezone", length=40, nullable=false)
    private String timezone = "America/Mexico_City";

    @Column(name="active", nullable=false)
    private boolean active = true;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt = Instant.now();
}

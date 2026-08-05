package com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities;

import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADFrequencyMode;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "head_prescription_medication",
        indexes = {
                @Index(name = "idx_prescription_med_prescription_id", columnList = "prescription_id")
        }
)
public class HEADPrescriptionMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private HEADPrescription prescription;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "dosage", length = 80)
    private String dosage;

    @Column(name = "frequency", length = 120)
    private String frequency;

    @Column(name = "duration", length = 80)
    private String duration;

    @Column(name = "instructions", length = 240)
    private String instructions;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_mode", length = 20)
    private HEADFrequencyMode frequencyMode; // ENUM STRING

    @Column(name = "times_per_day")
    private Integer timesPerDay; // nullable

    @Column(name = "interval_hours")
    private Integer intervalHours; // nullable


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "med_form", length = 16)
    private HEADMedicationForm medForm = HEADMedicationForm.TABLET;
}


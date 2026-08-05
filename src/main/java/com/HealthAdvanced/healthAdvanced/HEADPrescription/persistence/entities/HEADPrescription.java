package com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.entities;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "head_prescription",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_prescription_code", columnNames = "prescription_code"),
                @UniqueConstraint(name = "uk_prescription_job", columnNames = "job_id")
        },
        indexes = {
                @Index(name = "idx_prescription_client_uuid", columnList = "client_uuid"),
                @Index(name = "idx_prescription_doctor_uuid", columnList = "doctor_uuid"),
                @Index(name = "idx_prescription_issued_at", columnList = "issued_at")
        }
)
public class HEADPrescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescription_code", nullable = false, length = 40)
    private String prescriptionCode;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private HEADJob job;

    @Column(name = "client_uuid", nullable = false, length = 64)
    private String clientUuid;

    @Column(name = "doctor_uuid", nullable = false, length = 64)
    private String doctorUuid;

    // --- Doctor snapshot ---
    @Column(name = "doctor_name", nullable = false, length = 160)
    private String doctorName;

    @Column(name = "doctor_specialty", length = 120)
    private String doctorSpecialty;

    @Column(name = "doctor_license_no", length = 60)
    private String doctorLicenseNo;

    @Column(name = "doctor_clinic_name", length = 160)
    private String doctorClinicName;

    @Column(name = "doctor_clinic_address", length = 240)
    private String doctorClinicAddress;

    @Column(name = "doctor_phone", length = 40)
    private String doctorPhone;

    @Column(name = "doctor_email", length = 160)
    private String doctorEmail;

    // --- Patient snapshot ---
    @Column(name = "patient_name", nullable = false, length = 160)
    private String patientName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "patient_gender", length = 20)
    private String patientGender;

    @Column(name = "patient_address", length = 240)
    private String patientAddress;

    @Column(name = "date_text", length = 100)
    private String dateText;

    // --- Content ---
    @Lob
    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @Lob
    @Column(name = "additional_instructions")
    private String additionalInstructions;

    // NUEVO: síntomas como JSON
    @Lob
    @Column(name = "symptoms_json", columnDefinition = "LONGTEXT")
    private String symptomsJson;

    // NUEVO: fecha de seguimiento
    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    // NUEVO: notas
    @Lob
    @Column(name = "description")
    private String notes;

    // --- Signature vector (JSON string) ---
    @Lob
    @Column(name = "signature_vector_json", columnDefinition = "LONGTEXT")
    private String signatureVectorJson;

    @Column(name = "signature_signed_at")
    private Instant signatureSignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private HEADPrescriptionStatus status = HEADPrescriptionStatus.ISSUED;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "prescription",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("lineNo ASC")
    private List<HEADPrescriptionMedication> medications = new ArrayList<>();

    public void addMedication(HEADPrescriptionMedication m) {
        medications.add(m);
        m.setPrescription(this);
    }
}

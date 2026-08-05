package com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADCredentialType;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "staff_credentials",
        indexes = {
                @Index(name = "idx_cred_staff", columnList = "staff_user_id"),
                @Index(name = "idx_cred_staff_occ", columnList = "staff_user_id,occupation_profile_id"),
                @Index(name = "idx_cred_status", columnList = "status"),
                @Index(name = "idx_cred_type_status", columnList = "credential_type,status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cred_staff_occ_type",
                        columnNames = {"staff_user_id", "occupation_profile_id", "credential_type"}
                )
        }
)
public class HEADStaffCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Staff dueño de la credencial
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cred_staff_user"))
    private HEADPersonalUser staffUser;

    // Si la credencial depende del perfil (General, Auxiliar, Especialidad...)
    // Puede ser null si es "global" (en tu caso puedes decidir: general->profile o null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_profile_id", foreignKey = @ForeignKey(name = "fk_cred_occupation_profile"))
    private HEADOccupationProfile occupationProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 32)
    private HEADCredentialType credentialType = HEADCredentialType.LICENSE_NO;

    @Column(name = "value", nullable = false, length = 64)
    private String value;

    // Reutilizamos tu enum
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private HEADDocumentStatus status = HEADDocumentStatus.PENDING;

    // Opcional: ligar evidencia (doc aprobado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id", foreignKey = @ForeignKey(name = "fk_cred_source_document"))
    private HEADDocuments sourceDocument;

    private Long reviewedByAdminId;
    private LocalDateTime reviewedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void prePersist() {
        if (status == null) status = HEADDocumentStatus.PENDING;
        if (credentialType == null) credentialType = HEADCredentialType.LICENSE_NO;
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

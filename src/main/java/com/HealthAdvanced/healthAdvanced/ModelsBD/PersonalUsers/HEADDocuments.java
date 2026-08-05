package com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_docs_user", columnList = "id_user_id_user"),
                @Index(
                        name = "idx_docs_user_doc_occ",
                        columnList = "id_user_id_user,id_document_id_document,id_occupation_profile" // 👈 aquí
                )
        }
)
public class HEADDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocs;                 // id_docs

    // EXISTENTES
    private String nombreArchivo;        // nombre_archivo
    private String extension;            // extension

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HEADDocumentCatalogue idDocument;    // id_document_id_document

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HEADPersonalUser idUser;             // id_user_id_user

    // NUEVO: vínculo al archivo físico (file_assets.id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HEADFileAsset fileAsset;             // file_asset_id

    // NUEVO (opcional): documentos por perfil/ocupación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_occupation_profile", // 👈 aquí
            foreignKey = @ForeignKey(name = "fk_documents_occupation_profile")
    )
    private HEADOccupationProfile occupationProfile;

    // NUEVOS: metadatos/estado KYC
    private String mimeType;
    private Long sizeBytes;
    private String storageKey;
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private HEADDocumentStatus status = HEADDocumentStatus.PENDING;

    private String reviewNotes;
    private Long reviewedByAdminId;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime uploadedAt = java.time.LocalDateTime.now();
    private java.time.LocalDateTime reviewedAt;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    void prePersist() {
        if (status == null) status = HEADDocumentStatus.PENDING;
        if (uploadedAt == null) uploadedAt = java.time.LocalDateTime.now();
        if (active == null) active = true;
    }
}



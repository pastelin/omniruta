package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADScreenType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "file_assets")
public class HEADFileAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HEADOwnerType ownerType;      // SYSTEM | STAFF | CLIENT

    private Long ownerId;             // null si SYSTEM, o id del staff/cliente

    @Enumerated(EnumType.STRING)
    private HEADCategory category;        // BANNER, ICON, STAFF_ID, etc.

    private String storageKey;
    @Column(name = "url", columnDefinition = "TEXT") // objectName en Firebase (prefix + nombre archivo)
    private String url;               // URL pública o firmada (si guardas token)
    private String mimeType;          // image/png, application/pdf...
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    private HEADVisibility visibility;    // PUBLIC (banners) o PRIVATE (docs personales)

    private Boolean active = true;
    private Integer sortOrder = 0;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String Subtitle;

    @Column(length = 255)
    private String tags;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt = new java.util.Date();

    @Enumerated(EnumType.STRING)
    private HEADScreenType screenType;

    private Integer documentCatalogue;

    private String contentType;

    private Long contentLength;

}

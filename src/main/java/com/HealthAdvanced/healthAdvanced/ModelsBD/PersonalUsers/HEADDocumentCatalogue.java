package com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "documentCatalogue")
public class HEADDocumentCatalogue {
    @Id
    private Integer idDocument;
    private Integer idDocumentsRepeat;
    private String nameDocument;
    private String descriptionDocument;
    private String extension;
    @Column(columnDefinition = "bit(1)")
    private Boolean isSaveDocument;
    private String typeFile;
    @Column(columnDefinition = "bit(1)")
    private Boolean isVisibility;
    private Integer maxRepeats;

    @Column(columnDefinition = "bit(1)")
    private Boolean requiresLicenseNo;
}

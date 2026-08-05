package com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "documentOccupations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dococc_doc_occprofile",
                columnNames = {
                        "head_document_catalogue_id_document",
                        "head_occupation_profile_id_occupation_profile"
                }
        )
)
public class HEADDocumentOccupations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocumentOccupations;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HEADDocumentCatalogue headDocumentCatalogue;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HEADOccupationProfile headOccupationProfile;
    @Column(columnDefinition = "bit(1)")
    private Boolean isRequired;

}

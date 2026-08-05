package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.PersonalUsers.HEADDocuments;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_staff_certification",
        indexes = {
                @Index(name = "idx_staff_cert_staff", columnList = "staff_user_id,active,sort_order")
        })
public class HEADStaffCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "institution", length = 160)
    private String institution;

    @Column(name = "year_label", length = 8)
    private String year;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // opcional, por si después quieres adjuntar evidencia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "support_document_id")
    private HEADDocuments supportDocument;
}
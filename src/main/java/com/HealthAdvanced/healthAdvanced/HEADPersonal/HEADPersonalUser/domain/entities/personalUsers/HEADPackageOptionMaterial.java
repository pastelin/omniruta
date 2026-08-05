package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "package_option_materials")
public class HEADPackageOptionMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_option_id", nullable = false)
    private HEADPackageOption packageOption;

    @Column(nullable = false)
    private String materialName;
    // Ej: "Sonda Foley", "Guantes estériles", "Jeringa 10 ml"

    @Column(length = 80)
    private String quantityLabel;
    // Ej: "1 pieza", "2 pares", "1 caja"

    @Column(length = 255)
    private String notes;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
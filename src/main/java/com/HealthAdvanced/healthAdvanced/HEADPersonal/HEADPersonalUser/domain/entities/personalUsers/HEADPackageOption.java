package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "package_options")
public class HEADPackageOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private HEADPackagesPersonal pkg;

    @Column(nullable = false, length = 120)
    private String optionLabel;
    // Ej: "Con material", "Sin material"

    @Column(nullable = false)
    private Boolean includesMaterials = false;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceFrom;

    @Column(nullable = false, length = 10)
    private String currency = "MXN";

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;
}
package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stepSubCatalogue",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idCatalogue", "subStepName"}))
public class HEADStepSubCatalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCatalogue", nullable = false)
    private HEADStepCurrentCatalogue stepParent;

    @Column(nullable = false, length = 64)
    private String subStepName;

    @Column(nullable = false, length = 128)
    private String screenFlow;

    @Column(nullable = false)
    private Integer orderNo = 0;

    @Column(nullable = false)
    private Boolean required = true;
}

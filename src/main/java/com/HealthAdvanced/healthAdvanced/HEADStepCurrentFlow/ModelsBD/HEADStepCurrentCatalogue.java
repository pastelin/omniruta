package com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stepCatalogue", uniqueConstraints = @UniqueConstraint(columnNames = {"typeFlow", "stepName"}))
public class HEADStepCurrentCatalogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCatalogue;
    @Column(nullable = false)
    private String stepName;
    @Column(nullable = false)
    private String screenFlow;
    @Column(nullable = false)
    private String typeFlow;

    @Column(nullable = false)
    private Integer orderNo = 0;
    @Column(nullable = false)
    private Boolean required = true;
    @Column(length = 64)
    private String predicate;
}

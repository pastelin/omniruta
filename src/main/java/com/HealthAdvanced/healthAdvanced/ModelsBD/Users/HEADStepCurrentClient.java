package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;


import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stepCurrentClient",uniqueConstraints = @UniqueConstraint(columnNames = {"idUser","idCatalogue"}))
public class HEADStepCurrentClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStepCurrentClient;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private HEADClients idClient;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCatalogue", nullable = false)
    private HEADStepCurrentCatalogue idStepCatalogue;
    @Column(nullable = false)
    private Boolean isCompleteSteps = false;
}
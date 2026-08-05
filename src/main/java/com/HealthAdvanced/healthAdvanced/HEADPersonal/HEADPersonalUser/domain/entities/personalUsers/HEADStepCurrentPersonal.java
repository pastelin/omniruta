package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepCurrentCatalogue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stepCurrentPersonal")
public class HEADStepCurrentPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStepCurrentPersonal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private HEADPersonalUser idPersonalUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCatalogue", nullable = false)
    private HEADStepCurrentCatalogue idStepCatalogue;

    @Column(nullable = false)
    private Boolean isCompleteSteps = false;
}

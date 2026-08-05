package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "stepSubStatusPersonal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idUser", "idSub"})
)
public class HEADStepSubStatusPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private HEADPersonalUser idPersonalUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idSub", nullable = false)
    private HEADStepSubCatalogue sub;

    @Column(nullable = false)
    private Boolean isComplete = false;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void touch() { this.updatedAt = Instant.now(); }
}


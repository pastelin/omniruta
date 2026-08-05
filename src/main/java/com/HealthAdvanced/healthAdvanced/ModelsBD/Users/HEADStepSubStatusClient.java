package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;

import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stepSubStatusClient",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idUser", "idSub"}))
public class HEADStepSubStatusClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false)
    private HEADClients idClient;

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

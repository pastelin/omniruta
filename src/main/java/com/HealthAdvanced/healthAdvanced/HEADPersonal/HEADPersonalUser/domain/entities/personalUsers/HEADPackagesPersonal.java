package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "packagesAvailable")
public class HEADPackagesPersonal {
    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Column(nullable = false)
    private String title;

    private String subtitle;

    private String iconUrl;

    private Boolean active = true;
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private HEADServiceMode serviceMode = HEADServiceMode.HOME;

    @Column(nullable = false)
    private Integer serviceDurationMin = 45;

    @Column(nullable = false)
    private Boolean requiredPrescription = false;
}

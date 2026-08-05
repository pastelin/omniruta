package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "packagesToProfiles")
public class HEADPackagesToProfiles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPackagesToProfiles;
    private Boolean isActive;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_package_available",          // nombre de la columna FK en esta tabla
            referencedColumnName = "id"             // apunta a HEADPackagesPersonal.id (String)
    )
    private HEADPackagesPersonal idPackageAvailable;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_occupation_profile",         // tu FK al perfil
            referencedColumnName = "IdOccupationProfile"
    )
    private HEADOccupationProfile idOccupationProfile;
}

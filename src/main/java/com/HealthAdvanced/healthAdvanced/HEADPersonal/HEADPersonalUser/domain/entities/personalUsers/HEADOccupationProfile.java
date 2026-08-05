package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "occupationProfile")
public class HEADOccupationProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long IdOccupationProfile;
    private String nameTypeProfile; // General, Auxiliar, Cuidadora,
    @ManyToOne(fetch = FetchType.LAZY)
    private HEADOccupations idOccupation; //Medico, Enfermeria, etc
}

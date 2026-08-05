package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "occupationsPersonalUser")
public class HEHOOccupationPersonalUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOccupationPersonal;
    @ManyToOne(fetch = FetchType.LAZY)
    private HEADPersonalUser idPersonalUser;
    @ManyToOne(fetch = FetchType.LAZY)
    private HEADOccupationProfile idOccupationProfile;
}




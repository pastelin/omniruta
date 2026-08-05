package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "occupations")
public class HEADOccupations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOccupation;
    @Column(name = "nameOccupation")
    private String nameOccupation; //enfermeria, medico, especialidad
    private String nameImage;
    @Column(name = "icon_key")
    private String iconKey;
    @Enumerated(EnumType.STRING)
    @Column(name = "code", length = 32, nullable = false, unique = true)
    private HEADOccupationCode code;
}

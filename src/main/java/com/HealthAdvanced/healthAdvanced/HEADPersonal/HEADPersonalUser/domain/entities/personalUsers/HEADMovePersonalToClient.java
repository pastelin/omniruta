package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "moveStaffToClient")
public class HEADMovePersonalToClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idStaffToClient;
    @ManyToOne
    private HEADActiveLocationPersonal idActiveLocationPersonal;
    @ManyToOne
    private HEADServiceRequestClient idServiceRequestClient;
}

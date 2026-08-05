package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "sexUser")
public class HEADSexUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSexUser;
    private String typeSex;

}

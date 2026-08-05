package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_client_emergency_contact",
        uniqueConstraints = @UniqueConstraint(name="uk_emergency_client", columnNames = "client_id"))
public class HEADClientEmergencyContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private HEADClients client;

    @Column(name="full_name", length = 120)
    private String fullName;

    @Column(name="phone", length = 30)
    private String phone;

    @Column(name="relationship", length = 30)
    private String relationship; // opcional
}
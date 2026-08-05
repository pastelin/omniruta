package com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_client_medical_info",
        uniqueConstraints = @UniqueConstraint(name="uk_medical_client", columnNames = "client_id"))
public class HEADClientMedicalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private HEADClients client;

    @Column(name="blood_type", length = 3)
    private String bloodType;

    @Column(name="weight_kg")
    private Integer weightKg;

    @Column(name="height_cm")
    private Integer heightCm;
}
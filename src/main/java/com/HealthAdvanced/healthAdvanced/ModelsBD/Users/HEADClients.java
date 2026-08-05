package com.HealthAdvanced.healthAdvanced.ModelsBD.Users;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientEmergencyContact;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADMedicalContactEmergency.entity.HEADClientMedicalInfo;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "clientsUsers")
public class HEADClients {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String nombre;
    private String aPaterno;
    private String aMaterno;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @ManyToOne
    private HEADSexUser idSexUser;
    private String uuIdUser;
    private String roles;
    private Boolean IsAccepted;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    private HEADClientMedicalInfo medicalInfo;

    @OneToOne(mappedBy = "client", fetch = FetchType.LAZY)
    private HEADClientEmergencyContact emergencyContact;

    @Column(name = "google_sub", unique = true)
    private String googleSub;

    @Column(name = "auth_provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private HEADAuthProvider authProvider = HEADAuthProvider.LOCAL;

}
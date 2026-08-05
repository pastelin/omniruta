package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "auth_device",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
public class HEADAuthDevice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;                 // id numérico (clients_users/personal_user)

    @Column(name="platform", length=16)
    private String platform;             // ANDROID / IOS

    @Column(name="device_id", length=128, nullable=false)
    private String deviceId;             // identificador único del celular/app

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "uuidUser", length = 250,nullable = false)
    private String uuidUser;
}


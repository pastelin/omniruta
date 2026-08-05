package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.modelDB;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "auth_refresh_token",
        indexes = {
                @Index(name="idx_refresh_user_device", columnList = "user_id,device_id"),
                @Index(name="idx_refresh_token_hash", columnList = "token_hash")
        })
public class HEADAuthRefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="device_id", length=128, nullable=false)
    private String deviceId;

    @Column(name="token_hash", length=64, nullable=false)
    private String tokenHash;            // SHA-256 del refresh (nunca guardes el plano)

    @Column(name="issued_at", nullable=false)
    private LocalDateTime issuedAt;

    @Column(name="expires_at", nullable=false)
    private LocalDateTime expiresAt;     // tope absoluto (p.ej. 90 días)

    @Column(name="last_used_at")
    private LocalDateTime lastUsedAt; // para ventana deslizante (30 días)

    @Column(name = "uuidUser", length=250, nullable = false)
    private String uuidUser;

    @Column(name="revoked", nullable=false)
    private Boolean revoked = false;

    @Column(name="rotation_parent_id")
    private Long rotationParentId;       // para detectar reuse de tokens rotados
}

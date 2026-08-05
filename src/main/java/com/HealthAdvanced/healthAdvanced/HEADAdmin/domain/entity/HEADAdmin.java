package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.enums.HEADAdminRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "head_admins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_head_admin_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_head_admin_uid", columnNames = "uid_admin")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HEADAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid_admin", nullable = false, updatable = false, length = 36)
    private String uidAdmin;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 100)
    private HEADAdminRole role;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.uidAdmin == null || this.uidAdmin.isBlank()) {
            this.uidAdmin = UUID.randomUUID().toString();
        }
        if (this.active == null) {
            this.active = Boolean.TRUE;
        }
        if (this.role == null) {
            this.role = HEADAdminRole.ADMIN;
        }
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
}
package com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_user_legal_acceptance",
        indexes = {
                @Index(name = "idx_legal_accept_user", columnList = "user_type,user_id"),
                @Index(name = "idx_legal_accept_doc", columnList = "legal_document_id"),
                @Index(name = "idx_legal_accept_accepted_at", columnList = "accepted_at")
        })
public class HEADUserLegalAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 16)
    private HEADLegalUserType userType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "legal_document_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_legal_accept_document"))
    private HEADLegalDocument legalDocument;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted = true;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @Column(name = "app_version", length = 32)
    private String appVersion;

    @Column(name = "platform", length = 16)
    private String platform;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "language", length = 8)
    private String language;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @PrePersist
    void prePersist() {
        if (acceptedAt == null) acceptedAt = Instant.now();
        if (accepted == null) accepted = true;
    }
}
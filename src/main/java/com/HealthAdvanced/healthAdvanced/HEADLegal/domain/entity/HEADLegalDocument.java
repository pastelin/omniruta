package com.HealthAdvanced.healthAdvanced.HEADLegal.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalDocumentType;
import com.HealthAdvanced.healthAdvanced.HEADLegal.domain.enums.HEADLegalUserType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_legal_document",
        indexes = {
                @Index(name = "idx_legal_doc_user_type", columnList = "user_type,document_type,is_active"),
                @Index(name = "idx_legal_doc_version", columnList = "version"),
                @Index(name = "idx_legal_doc_published", columnList = "published_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_legal_doc_user_type_doc_type_version",
                        columnNames = {"user_type", "document_type", "version"}
                )
        })
public class HEADLegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 16)
    private HEADLegalUserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 24)
    private HEADLegalDocumentType documentType;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "version", nullable = false, length = 32)
    private String version;

    @Column(name = "content_url", length = 500)
    private String contentUrl;

    @Lob
    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "language", nullable = false, length = 8)
    private String language = "es";

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (publishedAt == null) publishedAt = now;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (isActive == null) isActive = true;
        if (language == null || language.isBlank()) language = "es";
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
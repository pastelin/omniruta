package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeExternalAccountType;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "staff_to_stripe_account",
        indexes = {
                @Index(name = "idx_staff_stripe_staff", columnList = "staff_user_id"),
                @Index(name = "idx_staff_stripe_account", columnList = "connected_account_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_staff_stripe_staff", columnNames = "staff_user_id"),
                @UniqueConstraint(name = "uk_staff_stripe_account", columnNames = "connected_account_id")
        }
)
public class HEADStaffToStripeAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Column(name = "connected_account_id", nullable = false, length = 128)
    private String connectedAccountId;

    @Column(name = "default_external_account_id", length = 128)
    private String defaultExternalAccountId;

    @Column(name = "payouts_enabled", nullable = false)
    private Boolean payoutsEnabled = false;

    @Column(name = "details_submitted", nullable = false)
    private Boolean detailsSubmitted = false;

    @Column(name = "charges_enabled", nullable = false)
    private Boolean chargesEnabled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "default_external_account_type")
    private HEADStripeExternalAccountType defaultExternalAccountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "stripe_status", nullable = false)
    private HEADStripeAccountStatus stripeStatus = HEADStripeAccountStatus.EMPTY;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (payoutsEnabled == null) payoutsEnabled = false;
        if (detailsSubmitted == null) detailsSubmitted = false;
        if (chargesEnabled == null) chargesEnabled = false;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
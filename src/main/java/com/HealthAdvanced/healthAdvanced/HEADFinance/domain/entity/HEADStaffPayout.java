package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPayoutPeriodType;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_staff_payout",
        indexes = {
                @Index(name = "idx_staff_payout_staff_status", columnList = "staff_user_id,status,requested_at"),
                @Index(name = "idx_staff_payout_period", columnList = "period_from,period_to")
        })
public class HEADStaffPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 24)
    private HEADPayoutPeriodType periodType;

    @Column(name = "period_from", nullable = false)
    private Instant periodFrom;

    @Column(name = "period_to", nullable = false)
    private Instant periodTo;

    @Column(name = "item_count", nullable = false)
    private Integer itemCount = 0;

    @Column(name = "gross_eligible_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossEligibleAmount;

    @Column(name = "adjustments_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal adjustmentsAmount;

    @Column(name = "final_payout_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalPayoutAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private HEADStaffPayoutStatus status;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "external_transfer_id", length = 255)
    private String externalTransferId;

    @Column(name = "external_payout_id", length = 255)
    private String externalPayoutId;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "external_payout_status", length = 32)
    private String externalPayoutStatus;

    @Column(name = "failure_code", length = 64)
    private String failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(name = "stripe_connected_account_id", length = 128)
    private String stripeConnectedAccountId;
}
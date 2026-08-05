package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFeeBearer;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_job_financial",
        indexes = {
                @Index(name = "idx_job_fin_staff", columnList = "staff_user_id,payout_status,payout_available_at"),
                @Index(name = "idx_job_fin_completed", columnList = "completed_at"),
                @Index(name = "idx_job_fin_processor", columnList = "processor"),
                @Index(name = "idx_job_fin_currency", columnList = "currency")
        })
public class HEADJobFinancial {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private HEADJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "processor", nullable = false, length = 32)
    private HEADPaymentProcessor processor;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "gross_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal grossAmount;

    @Column(name = "platform_fee_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal platformFeePercent;

    @Column(name = "platform_fee_fixed", precision = 12, scale = 2, nullable = false)
    private BigDecimal platformFeeFixed;

    @Column(name = "platform_fee_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal platformFeeAmount;

    @Column(name = "processor_fee_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal processorFeePercent;

    @Column(name = "processor_fee_fixed", precision = 12, scale = 2, nullable = false)
    private BigDecimal processorFeeFixed;

    @Enumerated(EnumType.STRING)
    @Column(name = "processor_fee_bearer", nullable = false, length = 16)
    private HEADFeeBearer processorFeeBearer;

    @Column(name = "direct_operational_cost_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal directOperationalCostAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "direct_cost_bearer", nullable = false, length = 16)
    private HEADFeeBearer directCostBearer;

    @Column(name = "withholding_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal withholdingAmount = BigDecimal.ZERO;

    @Column(name = "staff_payout_before_withholding", precision = 12, scale = 2, nullable = false)
    private BigDecimal staffPayoutBeforeWithholding;

    @Column(name = "staff_payout_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal staffPayoutAmount;

    @Column(name = "app_net_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal appNetAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", nullable = false, length = 24)
    private HEADJobPayoutStatus payoutStatus;

    @Column(name = "payout_available_at")
    private Instant payoutAvailableAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "platform_rule_id")
    private Long platformRuleId;

    @Column(name = "processor_rule_id")
    private Long processorRuleId;

    @Version
    private Long version;

    @Column(name = "processor_payment_intent_id", length = 128)
    private String processorPaymentIntentId;

    @Column(name = "processor_charge_id", length = 128)
    private String processorChargeId;

    @Column(name = "processor_balance_transaction_id", length = 128)
    private String processorBalanceTransactionId;

    @Column(name = "processor_fee_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal processorFeeAmount = BigDecimal.ZERO;

    @Column(name = "processor_net_amount", precision = 12, scale = 2)
    private BigDecimal processorNetAmount;

    @Column(name = "processor_fee_details_json", columnDefinition = "TEXT")
    private String processorFeeDetailsJson = "[]";

    @Column(name = "processor_fee_synced", nullable = false)
    private Boolean processorFeeSynced = false;

    @Column(name = "processor_fee_synced_at")
    private Instant processorFeeSyncedAt;
}
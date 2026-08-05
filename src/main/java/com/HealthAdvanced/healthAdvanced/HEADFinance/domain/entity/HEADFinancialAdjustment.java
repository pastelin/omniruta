package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFinancialAdjustmentKind;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFinancialTarget;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_financial_adjustment",
        indexes = {
                @Index(name = "idx_fin_adj_job", columnList = "job_id,created_at"),
                @Index(name = "idx_fin_adj_staff", columnList = "staff_user_id,created_at")
        })
public class HEADFinancialAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private HEADJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false, length = 16)
    private HEADFinancialTarget target; // STAFF / APP

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 24)
    private HEADFinancialAdjustmentKind kind; // BONUS, PENALTY, REFUND, CHARGEBACK, MANUAL

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
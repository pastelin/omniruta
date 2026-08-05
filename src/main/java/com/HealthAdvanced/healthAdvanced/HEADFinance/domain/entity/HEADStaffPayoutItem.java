package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "head_staff_payout_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payout_item_job", columnNames = "job_id")
        })
public class HEADStaffPayoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id", nullable = false)
    private HEADStaffPayout payout;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private HEADJob job;

    @Column(name = "base_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal baseAmount;

    @Column(name = "adjustments_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal adjustmentsAmount;

    @Column(name = "final_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalAmount;
}
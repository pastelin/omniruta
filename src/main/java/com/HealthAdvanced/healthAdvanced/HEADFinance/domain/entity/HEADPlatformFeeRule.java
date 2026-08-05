package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_platform_fee_rule")
public class HEADPlatformFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "platform_fee_percent", precision = 5, scale = 2, nullable = false)
    private BigDecimal platformFeePercent;

    @Column(name = "platform_fee_fixed", precision = 12, scale = 2, nullable = false)
    private BigDecimal platformFeeFixed = BigDecimal.ZERO;

    @Column(name = "hold_days", nullable = false)
    private Integer holdDays = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "min_duration_min")
    private Integer minDurationMin;

    @Column(name = "max_duration_min")
    private Integer maxDurationMin;
}

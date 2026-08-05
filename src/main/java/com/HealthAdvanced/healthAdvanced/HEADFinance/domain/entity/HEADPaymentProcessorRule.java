package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFeeBearer;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_payment_processor_rule")
public class HEADPaymentProcessorRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "processor", nullable = false, length = 32)
    private HEADPaymentProcessor processor;

    @Column(name = "percent_fee", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentFee;

    @Column(name = "fixed_fee", precision = 12, scale = 2, nullable = false)
    private BigDecimal fixedFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_bearer", nullable = false, length = 16)
    private HEADFeeBearer feeBearer;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;
}
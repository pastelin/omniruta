package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "head_operational_cost")
public class HEADOperationalCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String concept; // Oracle, Firebase, Dominio, etc.

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    private Instant periodFrom;
    private Instant periodTo;

    @Column(nullable = false)
    private Boolean recurring;
}

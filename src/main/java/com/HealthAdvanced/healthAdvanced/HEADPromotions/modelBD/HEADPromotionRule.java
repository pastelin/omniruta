package com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromoMetricKey;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromoOperator;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "promotion_rules",
        indexes = {
                @Index(name = "idx_rules_promo", columnList = "promotion_id"),
                @Index(name = "idx_rules_metric", columnList = "metric_key"),
                @Index(name = "idx_rules_enabled", columnList = "enabled")
        }
)
public class HEADPromotionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private HEADPromotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_key", nullable = false, length = 64)
    private HEADPromoMetricKey metricKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 16)
    private HEADPromoOperator operator;

    @Column(name = "value1", nullable = false, precision = 18, scale = 4)
    private BigDecimal value1;

    @Column(name = "value2", precision = 18, scale = 4)
    private BigDecimal value2; // solo BETWEEN

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;
}
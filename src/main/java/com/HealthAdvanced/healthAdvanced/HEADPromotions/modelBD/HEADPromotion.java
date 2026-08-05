package com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADUiActionType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name="promotions")
public class HEADPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private HEADPromotionTargetType targetType;        // CATEGORY | PACKAGE

    @Column(nullable=false, length=64)
    private String targetId;                       // id_occupation (como string) o slug del paquete

    @Column(nullable=false)
    private String label;                          // "Oferta", "10%", "Nuevo", etc.

    private Integer percent;                       // opcional (descuento), null si no aplica

    private LocalDateTime startsAt;                // null = ya
    private LocalDateTime endsAt;                  // null = sin fin

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private HEADPromotionStatus status = HEADPromotionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name="ui_variant", length=12)
    private HEADCardVariant uiVariant = HEADCardVariant.CARD;

    private Integer priority = 0;                  // para resolver conflictos (mayor = más importante)

    // (Opcional) segmentación:
    private String regionCode;                     // ej. "MX-CMX" o null para global
    private String notes;                          // admin description

    @Column(length = 255)
    private String audienceJson;

}

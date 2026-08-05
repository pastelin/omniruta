package com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADUiActionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "promotion_creative")
public class HEADPromotionCreative {

    @Id
    // Si lo quieres autogenerado:
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private HEADPromotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private HEADCardVariant variant; // BANNER/CARD/CHIP

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String iconUrl;

    @Column(length = 64)
    private String badgeLabel;

    @Column(columnDefinition = "TEXT")
    private String gradientJson; // ["#A855F7","#EC4899"]

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private HEADUiActionType action;

    @Column(columnDefinition = "TEXT")
    private String actionPayloadJson;

    private Integer sortKey;

    private Boolean enabled = true;
    private LocalDateTime startsAt;  // null = inmediato
    private LocalDateTime endsAt;    // null = sin fin
    private Integer priority = 0;    // por si hay conflicto
}
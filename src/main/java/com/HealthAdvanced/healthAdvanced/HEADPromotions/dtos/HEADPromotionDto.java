package com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardType;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADUiActionType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HEADPromotionDto {

    private HEADCardType cardType;      // BANNER | SERVICE | PROMO
    private String cardId;              // "BANNER:10", "SERVICE:2", "PROMO:101"

    private String title;
    private String subtitle;

    private String iconUrl;             // icono (servicios)
    private String imageUrl;            // imagen (banners/promos)

    private String priceLabel;          // "$450", "Desde $450"
    private String etaLabel;            // "7 min", "12–18 min", "Disponible ahora"

    private String badgeLabel;          // "Oferta", "10% OFF"

    private Boolean hasOffer;
    private Integer offerPercent;
    private String offerLabel;
    private String offerEndsAt;         // ISO8601 string

    private Integer providers;
    private Boolean availableNow;

    private HEADUiActionType action;    // OPEN_SERVICE | OPEN_PACKAGE | ...
    private HEADPromoTags actionPayload;

    private String section;
    private Integer sortKey;
}


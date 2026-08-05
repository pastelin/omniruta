package com.HealthAdvanced.healthAdvanced.HEADPromotions.map;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardType;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromotionDto;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionCreative;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.utils.HEADCreativeUtils;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.utils.HEADPromotionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HEADPromotionCreativeMapper {

    public HEADPromotionDto toDto(HEADPromotionCreative c) {

        HEADPromoTags payload = HEADPromotionUtils.parseTagsJson(c.getActionPayloadJson());
        if (payload == null) payload = new HEADPromoTags();

        var gradient = HEADCreativeUtils.parseGradient(c.getGradientJson());
        if ((payload.gradientHex == null || payload.gradientHex.isEmpty())
                && gradient != null && !gradient.isEmpty()) {
            payload.gradientHex = gradient;
        }

        var promo = c.getPromotion();
        boolean hasOffer = promo != null && promo.getPercent() != null;

        String offerEndsAt = (promo != null && promo.getEndsAt() != null)
                ? promo.getEndsAt().toString()
                : null;

        HEADCardType cardType = mapCardType(c);

        return HEADPromotionDto.builder()
                .cardType(cardType)
                .cardId("CREATIVE:" + c.getId())
                .title(c.getTitle())
                .subtitle(c.getSubtitle())
                .iconUrl(c.getIconUrl())
                .imageUrl(c.getImageUrl())
                .badgeLabel(firstNonBlank(c.getBadgeLabel(), hasOffer ? promo.getLabel() : null))

                .hasOffer(hasOffer)
                .offerPercent(hasOffer ? promo.getPercent() : null)
                .offerLabel(hasOffer ? promo.getLabel() : null)
                .offerEndsAt(offerEndsAt)

                .priceLabel(null)
                .etaLabel(null)
                .providers(null)
                .availableNow(null)

                .action(c.getAction())
                .actionPayload(payload)

                .section(sectionFor(cardType))
                .sortKey(c.getSortKey())
                .build();
    }

    private static HEADCardType mapCardType(HEADPromotionCreative c) {
        if (c.getVariant() == HEADCardVariant.BANNER) return HEADCardType.BANNER;
        if (c.getPromotion() != null) return HEADCardType.PROMO;
        return HEADCardType.BANNER;
    }

    private static String sectionFor(HEADCardType type) {
        return switch (type) {
            case BANNER -> "Banners";
            case PROMO -> "Promociones";
            case SERVICE -> "Servicios";
        };
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
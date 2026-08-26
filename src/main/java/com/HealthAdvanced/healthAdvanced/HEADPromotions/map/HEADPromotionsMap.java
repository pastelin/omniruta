package com.HealthAdvanced.healthAdvanced.HEADPromotions.map;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromotionDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity.HEADServiceRowResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADUiActionType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADContracts.HEADFileAssetSystemService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.HealthAdvanced.healthAdvanced.HEADPromotions.utils.HEADPromotionUtils.parseTagsJson;


@Service
public class HEADPromotionsMap {
    @Autowired
    private HEADFileAssetSystemService fileAssetService;

    public List<HEADPromotionDto> bannerMap(List<HEADFileAsset> fa) {
        var ref = new Object() {
            int i = 0;
        };
        return fa.stream().map(b -> {
            HEADPromoTags t = parseTagsJson(b.getTags());
            return HEADPromotionDto.builder()
                    .cardType(HEADCardType.BANNER)
                    .cardId(HEADCardType.BANNER.name() + ":" + b.getId())
                    .title(b.getTitle())
                    .subtitle(b.getSubtitle())
                    .imageUrl(b.getUrl())
                    .action(t != null ? t.action != null ? t.action : HEADUiActionType.NONE : HEADUiActionType.NONE)
                    .actionPayload(t)
                    .section("Banners")
                    .sortKey(10 + (ref.i++))
                    .build();
        }).toList();
    }

    public List<HEADPromotionDto> servicesCardsMap(List<HEADServiceRowResponse> services) {

        // Si todavía necesitas tags de icon asset por ownerId, construye mapa 1 sola vez:
        var fileAssets = fileAssetService.findByOwnerTypeAndCategory(HEADOwnerType.SYSTEM, HEADCategory.SERVICE_ICON);

        // ownerId -> asset (elige el primero por sortOrder asc ya viene ordenado si tú lo garantizas;
        // si no, aquí puedes ordenar antes)
        final var assetByOwnerId = fileAssets.stream()
                .filter(a -> a.getOwnerId() != null && a.getActive())
                .collect(Collectors.toMap(
                        HEADFileAsset::getOwnerId,
                        Function.identity(),
                        (a, b) -> a // si hay duplicados, quédate con el primero
                ));

        final var sectionName = "Servicios";

        // sortKey sin hack de ref object (usa AtomicInteger)
        final var sort = new java.util.concurrent.atomic.AtomicInteger(100);

        return services.stream().map(s -> {

            // Tags del icon (si aplica)
            var iconAsset = assetByOwnerId.get(s.id()); // OJO: aquí depende de qué sea s.id()
            // Si s.id() NO es el mismo ownerId que guardas en file_assets, esto nunca va a matchear.
            // (más abajo te digo cómo arreglarlo)
            HEADPromoTags tags = iconAsset != null ? parseTagsJson(iconAsset.getTags()) : null;

            // Badge sin forzar nulos
            String badge = Boolean.TRUE.equals(s.hasOffer())
                    ? (s.offerPercent() != null ? (s.offerPercent() + "% OFF") : "Oferta")
                    : null;

            return HEADPromotionDto.builder()
                    .cardType(HEADCardType.SERVICE)
                    .cardId("SERVICE:" + s.id())
                    .title(s.title())
                    .iconUrl(s.iconUrl())               // ya viene del service row
                    .imageUrl(null)                     // si luego agregas imagen grande por servicio, aquí va
                    .badgeLabel(badge)
                    .hasOffer(s.hasOffer())
                    .offerPercent(s.offerPercent())
                    .offerLabel(s.offerLabel())
                    .offerEndsAt(s.offerEndsAt() != null ? s.offerEndsAt().toString() : null)
                    .providers(s.providers())
                    .availableNow(s.availableNow())
                    .action(HEADUiActionType.OPEN_SERVICE)
                    .actionPayload(tags)
                    .priceLabel(s.priceLabel())
                    .etaLabel(s.etaLabel())
                    .section(sectionName)
                    .sortKey(sort.getAndIncrement())
                    .build();

        }).toList();
    }

    public List<HEADPromotionDto> buildPromoCard(List<HEADPromotion> promotions) {

        var ref = new Object() {
            int i = 200;
        };
        var promoCard = fileAssetService.findByOwnerTypeAndCategory(HEADOwnerType.SYSTEM,HEADCategory.PROMO_CARD);
        return promotions.stream().map(p -> {
            HEADPromoTags t = parseTagsJson(promoCard.stream().filter(pc -> pc.getOwnerId() == p.getId().intValue()).findFirst().orElse(new HEADFileAsset()).getTags());
            var builder = HEADPromotionDto.builder()
                    .cardType(HEADCardType.PROMO)
                    .cardId("PROMO:" + p.getId())
                    .title(p.getNotes())
                    .subtitle(p.getEndsAt() != null ? "Hasta " + p.getEndsAt().toLocalDate() : null)
                    .badgeLabel(p.getPercent() != null ? (p.getPercent() + "% OFF") : "Promo")
                    .hasOffer(true)
                    .offerPercent(p.getPercent())
                    .offerLabel(p.getLabel())
                    .offerEndsAt(p.getEndsAt() != null ? p.getEndsAt().toString() : null)
                    .section("Promociones")
                    .sortKey(ref.i++)
                    .actionPayload(t);
            if (p.getTargetType() == HEADPromotionTargetType.CATEGORY) {
                Integer occId = safeInt(p.getTargetId());
                builder.imageUrl(occId != null ? fileAssetService.promoImageForCategory(occId).orElse(null) : null)
                        .action(HEADUiActionType.OPEN_SERVICE);
            } else { // PACKAGE
                String slug = p.getTargetId();
                builder.imageUrl(fileAssetService.promoImageForPackage(slug).orElse(null))
                        .action(HEADUiActionType.OPEN_PACKAGE);
            }

            return builder.build();
        }).toList();
    }

    private static Integer safeInt(String s) {
        try { return Integer.valueOf(s); } catch (Exception e) { return null; }
    }

    @Transactional
    public HEADPromoTags buildServiceTag(Long profileId) {
        var getServiceInfo = fileAssetService.findByOwnerTypeAndCategory(HEADOwnerType.SYSTEM,HEADCategory.SERVICE_ICON);
        var asset = getServiceInfo.stream()
                .filter(assets -> profileId.equals(assets.getOwnerId()))
                .findFirst()
                .orElse(new HEADFileAsset());
        return parseTagsJson(asset.getTags());
    }
}

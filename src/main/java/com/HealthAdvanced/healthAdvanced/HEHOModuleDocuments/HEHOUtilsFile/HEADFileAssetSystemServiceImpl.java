package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADContracts.HEADFileAssetSystemService;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADVisibility;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HEADFileAssetSystemServiceImpl implements HEADFileAssetSystemService {
    @Autowired
    private HEADFileAssetRepository repo;

    // ---------- Públicos (implementación de la interfaz) ----------

    @Override
    @Cacheable(value = "assets", key = "'SERVICE_ICON:OCC:' + #occId", unless = "#result == null")
    public Optional<String> serviceIcon(Integer occId) {
        if (occId == null) return Optional.empty();
        return first(repo.findUrlsByCategoryAndTag(
                HEADCategory.SERVICE_ICON, tagOcc(occId), HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'PACKAGE_ICON:PKG:' + #packageSlug", unless = "#result == null")
    public Optional<String> packageIcon(String packageSlug) {
        if (isBlank(packageSlug)) return Optional.empty();
        return first(repo.findUrlsByCategoryAndTag(
                HEADCategory.PACKAGE_ICON, tagPkg(packageSlug), HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'PROMO_CARD:CATEGORY__' + #occId", unless = "#result == null")
    public Optional<String> promoImageForCategory(Integer occId) {
        if (occId == null) return Optional.empty();
        // Primera: por tag exacto CATEGORY__<id>; Fallback: por category sin tag
        var urls = repo.findUrlsByCategoryAndTag(
                HEADCategory.PROMO_CARD, "CATEGORY__" + occId, HEADVisibility.PUBLIC);
        if (!urls.isEmpty()) return Optional.of(urls.get(0));
        return first(repo.findUrlsByCategory(HEADCategory.PROMO_CARD, HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'PROMO_CARD:PACKAGE__' + #packageSlug", unless = "#result == null")
    public Optional<String> promoImageForPackage(String packageSlug) {
        if (isBlank(packageSlug)) return Optional.empty();
        var urls = repo.findUrlsByCategoryAndTag(
                HEADCategory.PROMO_CARD, "PACKAGE__" + packageSlug, HEADVisibility.PUBLIC);
        if (!urls.isEmpty()) return Optional.of(urls.get(0));
        return first(repo.findUrlsByCategory(HEADCategory.PROMO_CARD, HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'MAP_PIN_CLIENT'", unless = "#result == null")
    public Optional<String> mapPinClient() {
        return first(repo.findUrlsByCategory(HEADCategory.MAP_PIN_CLIENT, HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'MAP_PIN_STAFF'", unless = "#result == null")
    public Optional<HEADFileAsset> mapPinStaff(Long idOccupation) {
        return repo.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.SYSTEM,idOccupation,HEADCategory.MAP_PIN_STAFF);
    }

    @Override
    @Cacheable(value = "assets", key = "'MAP_PIN_SYSTEM'", unless = "#result == null")
    public Optional<String> mapPinSystem() {
        return first(repo.findUrlsByCategory(HEADCategory.MAP_PIN_SYSTEM, HEADVisibility.PUBLIC));
    }

    @Override
    @Cacheable(value = "assets", key = "'GENERIC:' + #category + ':' + #tag", unless = "#result == null")
    public Optional<String> findUrlForTag(HEADCategory category, String tag) {
        if (category == null || isBlank(tag)) return Optional.empty();
        return first(repo.findUrlsByCategoryAndTag(category, tag, HEADVisibility.PUBLIC));
    }

    @Override
    public List<HEADFileAsset> findByOwnerTypeAndCategory(HEADOwnerType headOwnerType, HEADCategory headCategory) {
         return repo.findByOwnerTypeAndCategoryAndActive(headOwnerType,headCategory,true);
    }
    // ---------- Helpers ----------

    private static Optional<String> first(List<String> urls) {
        return (urls == null || urls.isEmpty()) ? Optional.empty() : Optional.ofNullable(urls.get(0));
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }

    // Convenciones de tags
    private static String tagOcc(Integer occId) { return "OCC:" + occId; }
    private static String tagPkg(String slug)    { return "PKG:" + slug; }
}

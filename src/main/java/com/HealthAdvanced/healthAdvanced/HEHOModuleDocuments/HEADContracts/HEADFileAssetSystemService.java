package com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADContracts;

import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;

import java.util.List;
import java.util.Optional;

public interface HEADFileAssetSystemService {
    /** Ícono para categoría/ocupación (occId entero) */
    Optional<String> serviceIcon(Integer occId);

    /** Ícono para paquete (slug) */
    Optional<String> packageIcon(String packageSlug);

    /** Imagen de promo para categoría (occId) */
    Optional<String> promoImageForCategory(Integer occId);

    /** Imagen de promo para paquete (slug) */
    Optional<String> promoImageForPackage(String packageSlug);

    /** Pin de mapa (cliente/staff/sistema) */
    Optional<String> mapPinClient();
    Optional<HEADFileAsset> mapPinStaff(Long idOccupation);
    Optional<String> mapPinSystem();

    /** Buscador genérico por category + tag (CSV) */
    Optional<String> findUrlForTag(HEADCategory category, String tag);

    public List<HEADFileAsset> findByOwnerTypeAndCategory(HEADOwnerType headOwnerType, HEADCategory headCategory);
}

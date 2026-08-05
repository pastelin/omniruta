package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesMaps;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackageAvailableDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackageOptionAvailableDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackagesResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADServiceProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackageOptionRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HEADPackagesMaps {
    @Autowired
    private HEADPackagesToProfilesRepository headPackagesToProfilesRepository;
    @Autowired
    private HEADOccupationsProfilesRepository headOccupationsProfilesRepository;
    @Autowired
    private HEADOccupationsRepository headOccupationsRepository;
    @Autowired
    private HEADFileAssetRepository headFileAssetRepository;
    @Autowired
    private HEADPackageOptionRepository packageOptionRepository;

    @Transactional(readOnly = true)
    public HEADPackagesResponse packagesMapping(HEADStaffsActivesDto dto) {

        if (dto == null || dto.getHeadStaffsCurrents() == null || dto.getHeadStaffsCurrents().isEmpty()) {
            HEADPackagesResponse resp = new HEADPackagesResponse();
            resp.setPackageAvailableDtoList(List.of());
            return resp;
        }

        // 1) Extraer perfiles únicos
        List<Long> profileIds = dto.getHeadStaffsCurrents().stream()
                .map(HEADStaffActiveCurrent::getIdProfileUser)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (profileIds.isEmpty()) {
            HEADPackagesResponse resp = new HEADPackagesResponse();
            resp.setPackageAvailableDtoList(List.of());
            return resp;
        }

        // 2) Traer paquetes activos para esos perfiles
        List<HEADPackagesPersonal> rawPackages =
                headPackagesToProfilesRepository.findActivePackagesForProfiles(profileIds);

        if (rawPackages == null || rawPackages.isEmpty()) {
            HEADPackagesResponse resp = new HEADPackagesResponse();
            resp.setPackageAvailableDtoList(List.of());
            return resp;
        }

        // 3) Quitar duplicados por id de paquete
        Map<String, HEADPackagesPersonal> packagesById = rawPackages.stream()
                .collect(Collectors.toMap(
                        HEADPackagesPersonal::getId,
                        pkg -> pkg,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<HEADPackagesPersonal> packages = List.copyOf(packagesById.values());

        List<String> packageIds = packages.stream()
                .map(HEADPackagesPersonal::getId)
                .toList();

        // 4) Traer opciones activas de esos paquetes
        List<HEADPackageOption> options = packageOptionRepository.findAllActiveByPackageIds(packageIds);

        Map<String, List<HEADPackageOption>> optionsByPackageId = options.stream()
                .collect(Collectors.groupingBy(
                        opt -> opt.getPkg().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 5) Armar respuesta
        List<HEADPackageAvailableDto> list = packages.stream()
                .map(pkg -> {
                    List<HEADPackageOptionAvailableDto> optionDtos = optionsByPackageId
                            .getOrDefault(pkg.getId(), List.of())
                            .stream()
                            .map(option -> new HEADPackageOptionAvailableDto(
                                    option.getId(),
                                    option.getOptionLabel(),
                                    option.getIncludesMaterials(),
                                    option.getPriceFrom(),
                                    null, // crossedPrice
                                    option.getCurrency() != null ? option.getCurrency() : "MXN",
                                    null, // promoSource
                                    null, // discountPercent
                                    null  // promo
                            ))
                            .toList();

                    // Si no tiene opciones activas, no lo mostramos
                    if (optionDtos.isEmpty()) {
                        return null;
                    }

                    BigDecimal minPriceFrom = optionDtos.stream()
                            .map(HEADPackageOptionAvailableDto::getPriceFrom)
                            .filter(Objects::nonNull)
                            .min(BigDecimal::compareTo)
                            .orElse(null);

                    String currency = optionDtos.stream()
                            .map(HEADPackageOptionAvailableDto::getCurrency)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse("MXN");

                    return new HEADPackageAvailableDto(
                            pkg.getId(),
                            pkg.getTitle(),
                            pkg.getSubtitle(),
                            minPriceFrom,
                            currency,
                            pkg.getIconUrl(),
                            false, // isPopular
                            pkg.getServiceDurationMin(),
                            pkg.getRequiredPrescription(),
                            optionDtos
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        HEADPackagesResponse resp = new HEADPackagesResponse();
        resp.setPackageAvailableDtoList(list);
        return resp;
    }

    @Transactional(readOnly = true)
    public Set<Long> resolveProfileIdsFromPackage(String packageId) {
        if (packageId == null || packageId.isBlank()) return Set.of();
        var ids = headPackagesToProfilesRepository.findActiveProfileIdsByPackage(packageId);
        return (ids == null || ids.isEmpty()) ? Set.of() : new HashSet<>(ids);
    }

    @Transactional
    public HEADServiceProfileResponse getOccupationCurrent(Long serviceId, List<String> colors) {

        var getProfileOccupation = headOccupationsProfilesRepository.findById(serviceId).orElse(null);
        if (getProfileOccupation == null) {
            return null;
        }
        var getOccupation = headOccupationsRepository.findById(getProfileOccupation.getIdOccupation().getIdOccupation()).orElse(null);
        if (getOccupation == null) {
            return null;
        }

        String iconUrl = headFileAssetRepository
                .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                        HEADOwnerType.SYSTEM,
                        getProfileOccupation.getIdOccupationProfile(),
                        HEADCategory.SERVICE_ICON
                )
                .map(HEADFileAsset::getUrl)
                .orElse(null);
        return new HEADServiceProfileResponse(
                getProfileOccupation.getIdOccupationProfile(),
                getProfileOccupation.getNameTypeProfile(),
                colors,
                iconUrl
        );
    }

}

package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesService;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackageOptionRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.repository.HEADPromotionRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.service.HEADPromotionResolver;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.service.HEADPromotionsService;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesMaps.HEADPackagesMaps;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.service.HEADShowStaffsToClientsService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADErrorCommonsSocket;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HEADPackagesService {

    @Autowired
    private HEADShowStaffsToClientsService headShowStaffsToClientsService;
    @Autowired
    private HEADJwtGenerator headJwtGenerator;
    @Autowired
    private HEADPackagesMaps headPackagesMaps;

    @Autowired
    private HEADPromotionsService headPromotionsService;

    @Autowired
    private HEADPackagesPersonalRepository headPackagesPersonalRepository;

    @Autowired private HEADPromotionResolver promotionsResolver;

    @Autowired
    private HEADPackageOptionRepository packageOptionRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> showPackagesAvailable(HEADClientLocationPackage headClientLocationCurrent) {
        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        HEADStaffsActivesDto getUsers = headShowStaffsToClientsService.getStaffsForClient(getUUID,headClientLocationCurrent);
        if (getUsers == null) {
            var errorSocket = new HEADErrorCommonsSocket();
            errorSocket.setCode(400);
            errorSocket.setIsSuccess(false);
            errorSocket.setMessage("No eres cliente, favor de ingresar tus datos correctamente");
            return new ResponseEntity<>(errorSocket, HttpStatus.UNAUTHORIZED);
        }
        HEADPackagesResponse packageDtoList = headPackagesMaps.packagesMapping(getUsers);
        return new ResponseEntity<>(packageDtoList, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public HEADPackageAvailableResponse listByProfile(Long profileId) {

        var packages = headPackagesPersonalRepository.findAllByProfile(profileId);
        var packageIds = packages.stream()
                .map(HEADPackagesPersonal::getId)
                .toList();

        var options = packageOptionRepository.findAllActiveByPackageIds(packageIds);

        var optionsByPackageId = options.stream()
                .collect(Collectors.groupingBy(opt -> opt.getPkg().getId()));

        var resolved = promotionsResolver.resolveForProfileAndPackages(profileId, packageIds);
        var popularIds = headPromotionsService.resolvePopularPackageIds(packages);

        var packagesCurrent = packages.stream()
                .map(pkg -> {
                    var profilePromo = resolved.bestProfilePromo();
                    var packagePromo = resolved.bestByPackage().get(pkg.getId());
                    var bestPromo = pickBestPromoForPackage(profilePromo, packagePromo);

                    Integer percent = bestPromo != null ? bestPromo.getPercent() : null;
                    boolean isPopular = popularIds.contains(pkg.getId());

                    var optionDtos = optionsByPackageId.getOrDefault(pkg.getId(), List.of())
                            .stream()
                            .map(option -> {
                                BigDecimal basePrice = option.getPriceFrom();
                                BigDecimal finalPrice = basePrice;
                                BigDecimal crossedPrice = null;

                                if (basePrice != null && percent != null && percent > 0) {
                                    crossedPrice = basePrice;
                                    finalPrice = basePrice.subtract(
                                            basePrice.multiply(BigDecimal.valueOf(percent))
                                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                    );
                                }

                                return new HEADPackageOptionAvailableDto(
                                        option.getId(),
                                        option.getOptionLabel(),
                                        option.getIncludesMaterials(),
                                        finalPrice,
                                        crossedPrice,
                                        option.getCurrency() != null ? option.getCurrency() : "MXN",
                                        bestPromo != null ? bestPromo.getTargetType().name() : null,
                                        percent,
                                        headPromotionsService.toDto(bestPromo)
                                );
                            })
                            .toList();

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
                            isPopular,
                            pkg.getServiceDurationMin(),
                            pkg.getRequiredPrescription(),
                            optionDtos
                    );
                })
                .toList();

        HEADPromoTags tagCurrent = headPromotionsService.getTagsServices(profileId);
        // Gradiente de respaldo si el perfil no tiene tags configurados (sin icon_key/tags en file_assets)
        List<String> gradientHex = (tagCurrent != null && tagCurrent.gradientHex != null)
                ? tagCurrent.gradientHex
                : List.of("#2563EB", "#1868DB");
        HEADServiceProfileResponse profile =
                headPackagesMaps.getOccupationCurrent(profileId, gradientHex);

        return new HEADPackageAvailableResponse(profile, packagesCurrent);
    }

    private HEADPromotion pickHigherPriority(HEADPromotion a, HEADPromotion b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.getPriority() >= b.getPriority() ? a : b;
    }

    public static HEADPromotion pickBestPromoForPackage(HEADPromotion promoProfile, HEADPromotion promoPackage) {
        if (promoProfile == null) return promoPackage;
        if (promoPackage == null) return promoProfile;

        int pProfile = Optional.ofNullable(promoProfile.getPriority()).orElse(0);
        int pPackage = Optional.ofNullable(promoPackage.getPriority()).orElse(0);

        if (pPackage > pProfile) return promoPackage;
        if (pProfile > pPackage) return promoProfile;


        return promoPackage;
    }
}

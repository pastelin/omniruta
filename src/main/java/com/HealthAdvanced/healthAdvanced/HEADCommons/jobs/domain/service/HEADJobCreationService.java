package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient.HEADClientWebSocketRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service.HEADGeocodingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackageOptionRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.service.HEADPromotionResolver;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentStripeAmountRequest;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesService.HEADPackagesService.pickBestPromoForPackage;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADJobCreationService {
    private final HEADClientsRepository clientsRepo;
    private final HEADPackagesPersonalRepository packagesRepo;
    private final HEADClientWebSocketRepository serviceReqRepo;
    private final HEADGeocodingService geocodingService;
    private final HEADJobRepository jobRepo;
    private final HEADPromotionResolver promotionsResolver;
    private final HEADOccupationsProfilesRepository occupationsProfilesRepository;
    private final HEADPackageOptionRepository packageOptionRepository;
    private final HEADFileAssetRepository repoFileAsset;



    @Transactional
    public HEADJob createAndSaveNewJob(HEADPaymentStripeAmountRequest req, String clientUuid) {

        var client = clientsRepo.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new HEADBadRequestException("Client not found: " + clientUuid));

        var option = packageOptionRepository.findActiveByIdWithPackage(req.getPackageOptionId())
                .orElseThrow(() -> new HEADBadRequestException(
                        "Package option not found: " + req.getPackageOptionId()
                ));

        var pkg = option.getPkg();

        if (req.getIdPackage() != null && !pkg.getId().equals(req.getIdPackage())) {
            throw new HEADBadRequestException("La opción seleccionada no pertenece al paquete enviado");
        }

        var existsPackageProfile = occupationsProfilesRepository.findById(req.getProfileId())
                .orElseThrow(() -> new HEADBadRequestException("El Servicio no existe: " + req.getProfileId()));

        String startAddr = null;
        try {
            startAddr = geocodingService.getAddressDescription(req.getUserLat(), req.getUserLong());
        } catch (Exception ignore) {
            log.info("startNewJobAssignment error geocoding={}", ignore.getMessage());
        }

        log.info("startNewJobAssignment va al servicio a obtener las direcciones domain={}", startAddr);

        var resolved = promotionsResolver.resolveForProfileAndPackages(
                existsPackageProfile.getIdOccupationProfile(),
                List.of(pkg.getId())
        );

        var profilePromo = resolved.bestProfilePromo();
        var packagePromo = resolved.bestByPackage().get(pkg.getId());
        var bestPromo = pickBestPromoForPackage(profilePromo, packagePromo);

        // Precio ahora sale de la OPTION
        BigDecimal basePrice = option.getPriceFrom();
        BigDecimal finalPrice = basePrice;

        Integer percent = bestPromo != null ? bestPromo.getPercent() : null;
        if (basePrice != null && percent != null && percent > 0) {
            finalPrice = basePrice.subtract(
                    basePrice.multiply(BigDecimal.valueOf(percent))
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP)
            );
        }

        String currency = option.getCurrency() != null ? option.getCurrency() : "MXN";

        // ----- ServiceRequest (sí se persiste siempre) -----
        var sr = new HEADServiceRequestClient();
        sr.setPkg(pkg);
        sr.setPackageOption(option); // NUEVO
        sr.setLatitude(req.getUserLat());
        sr.setLongitude(req.getUserLong());
        sr.setIdClient(client);
        sr.setUuIdUser(clientUuid);
        sr.setDateCurrent(java.time.OffsetDateTime.now().toString());
        sr.setStartAddress(startAddr);
        sr.setEndAddress(null);
        sr.setAmount(finalPrice);
        sr.setCurrency(currency);
        sr.setIdProfile(req.getProfileId());
        if (pkg.getRequiredPrescription()) {
            var clientPrescription = repoFileAsset.findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(HEADOwnerType.CLIENT, client.getIdUser(), HEADCategory.PRESCRIPTION).orElse(null);
            sr.setPrescriptionAsset(clientPrescription);
        }
        sr = serviceReqRepo.save(sr);
        log.info("startNewJobAssignment guardó ServiceRequestClient id={} packageId={} optionId={}",
                sr.getIdServiceRequestClient(), pkg.getId(), option.getId());

        // ----- Job (solo en memoria, aún NO se guarda) -----
        var job = new HEADJob();
        job.setRequest(sr);
        job.setClient(client);

        job.setState(HEADJobState.PENDING_ASSIGNMENT);

        job.setClientLat(req.getUserLat());
        job.setClientLng(req.getUserLong());
        job.setStartAddress(startAddr);
        job.setEndAddress(null);

        // Monto final ahora sale de la OPTION + promo
        job.setAmount(finalPrice);
        job.setCurrency(currency);

        job.setPaymentStatus(HEADPaymentStatus.NONE);
        job.setPaymentIntentId(null);
        job.setStripeStatusRaw(null);
        job.setCapturedAt(null);
        job.setSettledAt(null);
        job.setServiceDurationMin(pkg.getServiceDurationMin());

        // Esto sigue viviendo en el package base
        job.setServiceMode(pkg.getServiceMode());

        return job;
    }
}


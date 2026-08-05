package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesMaps.HEADPackagesMaps;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffActiveCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps.HEADShowStaffsToClientsMap;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient.HEADClientWebSocketRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.service.HEADGeocodingService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADRideAssignmentService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADPaymentStatus;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.PaymentService.HEADStripeJobGuardService;
import com.HealthAdvanced.healthAdvanced.HEHOPaymentMethods.stripe.entity.request.HEADPaymentStripeAmountRequest;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADShowStaffsToClientsService {

    private final HEADClientsRepository headClientsRepository;
    private final HEADJwtGenerator headJwtGenerator;

    private final HEADShowStaffsToClientsMap headShowStaffsToClientsMap;

    private final HEADRideAssignmentService rideAssignmentService;
    private final HEADJobRepository jobRepository;
    private final HEADPackagesMaps packageDirectory;
    private final HEADStripeJobGuardService stripeJobGuard;

    // --- Mostrar staffs cercanos (para UI del mapa) ---
    @Transactional(readOnly = true)
    public HEADStaffsActivesDto getNearbyStaffsForMap(HEADClientLocationPackage req,boolean onlyAvailable) {
        // 1) Perfiles admitidos por el paquete (si viene)
        Set<Long> profileFilter = packageDirectory.resolveProfileIdsFromPackage(req.getIdPackage());

        // 2) Snapshot nearby usando el store en memoria (staffStateStore) + repos de perfiles
        return headShowStaffsToClientsMap.staffWithinRadiusDtoFastMulti(
                req.getUserLat(),
                req.getUserLong(),
                /* limit        */ 80,
                /* useEta       */ true,
                /* avgSpeedKmh  */ 25d,
                /* filterIds    */ profileFilter,
                onlyAvailable
        );
    }

    @Transactional
    public HEADJob startNewJobAssignment(HEADClientLocationPackage req, String clientUuid) {

        HEADJob job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new HEADBadRequestException("Servicio no encontrado"));

        if (job.getClient() == null || !Objects.equals(job.getClient().getUuIdUser(), clientUuid)) {
            throw new HEADBadRequestException("No tienes permisos sobre este servicio");
        }
        stripeJobGuard.assertJobNotCanceledInStripe(job);

        var nearby = getNearbyStaffsForMap(req, true);

        List<String> queue = (nearby == null || nearby.getHeadStaffsCurrents() == null)
                ? List.of()
                : nearby.getHeadStaffsCurrents().stream()
                .map(HEADStaffActiveCurrent::getUuidUser)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        rideAssignmentService.startAssignmentProcess(job.getId(), queue);

        return job;
    }


    // --- (opcional) Endpoint “legacy” que antes usaba profileToStaffsMap ---
    // Si tenías algo como “setStaffsToClientCurrent”, ahora simplemente pide nearby:
    @Transactional(readOnly = true)
    public HEADStaffsActivesDto getStaffsForClient(String clientUuid, HEADClientLocationPackage req) {
        // Solo redirige al nearby unificado:
        return getNearbyStaffsForMap(req,true);
    }
}

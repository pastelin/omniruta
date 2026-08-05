package com.HealthAdvanced.healthAdvanced.HEADPromotions.service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.entity.*;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardType;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADCardVariant;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository.HEADNotificationInboxRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromoTags;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos.HEADPromotionDto;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.enums.HEADPromotionTargetType;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.map.HEADPromotionCreativeMapper;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.map.HEADPromotionsMap;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotion;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.modelBD.HEADPromotionCreative;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.repository.HEADPromotionCreativeRepository;
import com.HealthAdvanced.healthAdvanced.HEADPromotions.repository.HEADPromotionRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.dtos.HEADPromoDTO;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.maps.HEADShowStaffsToClientsMap;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Repository.HEADGeolocationRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesPersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Repository.HEADOccupationsRepository;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADPrescriptionStatus;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.persistence.repositories.HEADPrescriptionJpaRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class HEADPromotionsService {

    @Autowired private HEADJwtGenerator headJwtGenerator;
    @Autowired private HEADFileAssetRepository headFileAssetRepository;
    @Autowired private HEADPromotionsMap headPromotionsMap;
    @Autowired private HEADOccupationsRepository headOccupationsRepo;
    @Autowired private HEADPromotionRepository headPromotionRepository;
    @Autowired private HEADGeolocationRepository headGeolocationRepository;
    @Autowired private HEADPackagesToProfilesRepository headPackagesToProfilesRepository;
    @Autowired private HEADOccupationsProfilesRepository headOccupationsProfilesRepository;
    @Autowired private HEADJobRepository headJobRepository;
    @Autowired private HEADPrescriptionJpaRepository headPrescriptionRepository;
    @Autowired private HEADClientsRepository clientsRepo;
    @Autowired private HEADShowStaffsToClientsMap headShowStaffsToClientsMap;
    @Autowired private HEADPackagesPersonalRepository headPackagesPersonalRepository;
    @Autowired private HEADPromotionCreativeRepository creativeRepo;
    @Autowired private HEADPromotionCreativeMapper creativeMapper;
    @Autowired private HEADNotificationInboxRepository repoNotify;

    // --------------------------------------------------------------------------------------------
    // Endpoint legacy (si lo sigues usando)
    // --------------------------------------------------------------------------------------------
    public ResponseEntity<?> listServices() {
        var catalog = headOccupationsRepo.findAll(); // o findCatalog()
        var occIds = catalog.stream()
                .map(HEADOccupations::getIdOccupation)
                .map(String::valueOf)
                .toList();

        var promos = headPromotionRepository.findActiveForTargets(
                HEADPromotionTargetType.CATEGORY,
                occIds,
                LocalDateTime.now(),
                HEADPromotionStatus.ACTIVE
        );

        Map<Integer, HEADPromotion> promoByOcc = promos.stream()
                .collect(Collectors.toMap(
                        p -> Integer.valueOf(p.getTargetId()),
                        Function.identity(),
                        (a, b) -> a.getPriority() >= b.getPriority() ? a : b
                ));

        var items = catalog.stream()
                .map(o -> {
                    var promo = promoByOcc.get(o.getIdOccupation());
                    boolean hasOffer = promo != null;
                    String offerLabel = hasOffer ? promo.getLabel() : null;

                    return HEADServiceCardDto.builder()
                            .id(o.getIdOccupation())
                            .title(o.getNameOccupation())
                            .iconUrl(null)
                            .availableNow(false)
                            .providers(0)
                            .hasOffer(hasOffer)
                            .offerLabel(offerLabel)
                            .build();
                })
                .toList();

        return new ResponseEntity<>(
                HEADServicesCardsResponse.builder().items(items).build(),
                HttpStatus.OK
        );
    }

    // --------------------------------------------------------------------------------------------
    // DASHBOARD (nuevo endpoint que consumes en el front)
    // --------------------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public HEADDashboardInfoResponse getDashboardCards(Double lat, Double lng, Double radiusKm) {

        String getUUID = headJwtGenerator.getUserNamePersonalUser();
        var client = clientsRepo.findByUuIdUser(getUUID).orElse(null);
        if (client == null) {
            return null;
        }
        var stateInfo = buildStats(client);
        // BANNER = carrusel
        var banners = buildBannerCards();

        // SERVICE = grid
        var services = buildServiceCards(lat, lng, radiusKm);

        // PROMO = SpecialOfferBanner -> 1 sola promo
        var specialOffer = buildSpecialOfferPromoCard();

        var promotionsInfo = Stream.of(banners, services, specialOffer)
                .flatMap(List::stream)
                .sorted(Comparator
                        .comparing(HEADPromotionDto::getSection, Comparator.nullsLast(String::compareTo))
                        .thenComparing(c -> c.getSortKey() == null ? Integer.MAX_VALUE : c.getSortKey()))
                .toList();

        var eta = etaCurrent(lat,lng);

        long total = repoNotify.countByUserUuidAndDeletedFalse(getUUID);
        long unread = repoNotify.countByUserUuidAndDeletedFalseAndIsReadFalse(getUUID);
        var countNotifications = new HEADNotificationsResponse.Summary(total,unread);
        return new HEADDashboardInfoResponse(countNotifications,eta + " min",stateInfo, promotionsInfo);
    }

    private List<HEADPromotionDto> buildBannerCards() {
        var now = LocalDateTime.now();

        return creativeRepo.findActiveByVariant(HEADCardVariant.BANNER, now).stream()
                .map(creativeMapper::toDto)
                .peek(dto -> {
                    dto.setCardType(HEADCardType.BANNER);
                    dto.setSection("Banners");
                    if (dto.getSortKey() == null) dto.setSortKey(10);
                })
                .sorted(Comparator.comparingInt(d -> d.getSortKey() == null ? 999999 : d.getSortKey()))
                .toList();
    }

    // ---------- 2) SERVICES ----------
    private List<HEADPromotionDto> buildServiceCards(Double lat, Double lng, Double radiusKm) {
        var rows = this.getServices(lat, lng, radiusKm);
        // mapper ya setea cardType=SERVICE, section=Servicios, iconUrl, offers, etc.
        return headPromotionsMap.servicesCardsMap(rows);
    }

    private List<HEADPromotionDto> buildSpecialOfferPromoCard() {
        var now = LocalDateTime.now();

        return creativeRepo.findActiveByVariant(HEADCardVariant.CARD, now).stream()
                .filter(c -> c.getPromotion() != null)
                .findFirst()
                .map(creativeMapper::toDto) // aquí ya cardType= PROMO por tu mapCardType()
                .map(List::of)
                .orElseGet(List::of);
    }

    // --------------------------------------------------------------------------------------------
    // SERVICES ROWS (tu cálculo de disponibilidad + promos + iconUrl)
    // --------------------------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<HEADServiceRowResponse> getServices(Double lat, Double lng, Double radiusKm) {

        // 1) Catálogo fijo (perfiles)
        final var catalog = headOccupationsProfilesRepository.findAll();

        // 2) Disponibilidad (dedupe por staff)
        final Map<Long, Long> providersByOcc;

        if (lat == null || lng == null) {
            providersByOcc = Collections.emptyMap();
        } else {
            final double r = (radiusKm != null ? radiusKm : 5.0);
            final double latDelta = r / 111.0;
            final double lngDelta = r / (111.0 * Math.cos(Math.toRadians(lat)));
            final double latMin = lat - latDelta, latMax = lat + latDelta;
            final double lngMin = lng - lngDelta, lngMax = lng + lngDelta;

            final var candidates = headGeolocationRepository.findActiveInBBoxWithOcc(latMin, latMax, lngMin, lngMax);

            providersByOcc = candidates.stream()
                    .filter(alp -> alp.getLatitude() != null && alp.getLongitude() != null)
                    .filter(alp -> haversineKm(lat, lng, alp.getLatitude(), alp.getLongitude()) <= r)
                    .flatMap(alp -> {
                        var pu = alp.getIdPersonalUser();
                        if (pu == null) return Stream.empty();
                        var links = pu.getOccupationLinks();
                        if (links == null || links.isEmpty()) return Stream.empty();

                        return links.stream()
                                .map(HEHOOccupationPersonalUser::getIdOccupationProfile)
                                .filter(Objects::nonNull)
                                .map(HEADOccupationProfile::getIdOccupationProfile)
                                .filter(Objects::nonNull)
                                .map(occId -> Map.entry(occId, pu.getIdUser())); // (occProfileId, staffId)
                    })
                    .collect(Collectors.groupingBy(
                            Map.Entry::getKey,
                            Collectors.mapping(
                                    Map.Entry::getValue,
                                    Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                            )
                    ));
        }

        // 3) Promos activas (merge por prioridad)
        final var now = LocalDateTime.now();
        final var promos = headPromotionRepository.findActive(now, HEADPromotionStatus.ACTIVE);

        Map<Integer, HEADPromotion> bestPromoByOcc =
                promos.stream()
                        .filter(p -> p.getTargetType() == HEADPromotionTargetType.CATEGORY)
                        .map(p -> Map.entry(safeInt(p.getTargetId()), p))
                        .filter(e -> e.getKey() != null)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a.getPriority() >= b.getPriority() ? a : b
                        ));

        promos.stream()
                .filter(p -> p.getTargetType() == HEADPromotionTargetType.PACKAGE)
                .forEach(p -> {
                    var occIds = headPackagesToProfilesRepository.findOccupationIdsByPackageSlug(p.getTargetId());
                    occIds.forEach(occId ->
                            bestPromoByOcc.merge(occId, p, (a, b) -> a.getPriority() >= b.getPriority() ? a : b)
                    );
                });

        // 4) Construir respuesta
        return catalog.stream()
                .map(o -> {
                    int providers = Math.toIntExact(
                            providersByOcc.getOrDefault(o.getIdOccupationProfile(), 0L)
                    );

                    var promo = bestPromoByOcc.get(o.getIdOccupationProfile().intValue());
                    boolean hasOffer = (promo != null);

                    Integer percent = hasOffer ? promo.getPercent() : null;
                    var endsAt = hasOffer ? promo.getEndsAt() : null;

                    // iconUrl por ocupación (si tu ownerId es occupationId)
                    String iconUrl = headFileAssetRepository
                            .findFirstByOwnerTypeAndOwnerIdAndCategoryAndActiveTrue(
                                    HEADOwnerType.SYSTEM,
                                    o.getIdOccupationProfile(),
                                    HEADCategory.SERVICE_ICON
                            )
                            .map(HEADFileAsset::getUrl)
                            .orElse(null);

                    Integer occId = o.getIdOccupation().getIdOccupation();

                    BigDecimal min = headPackagesPersonalRepository.findMinPriceFromByProfile(o.getIdOccupationProfile());
                    String priceLabel = toPriceLabel(min);

                    return new HEADServiceRowResponse(
                            o.getIdOccupationProfile(),                                  // id (profile)
                            occId,                                                       // occupationId
                            o.getNameTypeProfile(),
                            providers,
                            providers > 0,
                            hasOffer,
                            hasOffer ? promo.getLabel() : null,
                            iconUrl,
                            priceLabel,
                            etaTime(lat,lng, o.getIdOccupationProfile()),
                            percent,
                            endsAt
                    );
                })
                .sorted(
                        Comparator
                                .comparing((HEADServiceRowResponse s) -> Boolean.TRUE.equals(s.hasOffer()) ? 0 : 1)
                                .thenComparing(s -> Boolean.TRUE.equals(s.availableNow()) ? 0 : 1)
                                .thenComparing((HEADServiceRowResponse s) -> -s.providers())
                                .thenComparing(HEADServiceRowResponse::title)
                )
                .toList();
    }

    // --------------------------------------------------------------------------------------------
    // Utils
    // --------------------------------------------------------------------------------------------
    private static Integer safeInt(String s) {
        try { return Integer.valueOf(s); } catch (Exception e) { return null; }
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    // Si todavía usas estos helpers en otros lados, déjalos:
    public HEADPromoDTO toDto(HEADPromotion p) {
        if (p == null) return null;
        return new HEADPromoDTO(
                p.getLabel(),
                p.getPercent(),
                p.getNotes(),
                p.getPriority()
        );
    }

    public Map<String, HEADPromotion> buildBestPromoByPackage() {
        var now = LocalDateTime.now();
        return headPromotionRepository.findActive(now, HEADPromotionStatus.ACTIVE).stream()
                .filter(p -> p.getTargetType() == HEADPromotionTargetType.PACKAGE)
                .collect(Collectors.toMap(
                        HEADPromotion::getTargetId, // idPackageAvailable
                        Function.identity(),
                        this::pickHigherPriorityPromotion
                ));
    }

    public Map<Integer, HEADPromotion> buildBestPromoByOccupationProfile() {
        var now = LocalDateTime.now();
        return headPromotionRepository.findActive(now, HEADPromotionStatus.ACTIVE).stream()
                .filter(p -> p.getTargetType() == HEADPromotionTargetType.CATEGORY) // si CATEGORY representa profile
                .map(p -> Map.entry(Objects.requireNonNull(safeParseInt(p.getTargetId())), p))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,      // idOccupationProfile
                        Map.Entry::getValue,
                        this::pickHigherPriorityPromotion
                ));
    }

    private HEADPromotion pickHigherPriorityPromotion(HEADPromotion a, HEADPromotion b) {
        if (a == null) return b;
        if (b == null) return a;
        int pa = Optional.ofNullable(a.getPriority()).orElse(0);
        int pb = Optional.ofNullable(b.getPriority()).orElse(0);
        return pa >= pb ? a : b;
    }

    private Integer safeParseInt(String value) {
        try { return Integer.valueOf(value); }
        catch (Exception e) { return null; }
    }

    private HEADDashboardStatsDto buildStats(HEADClients client) {

        var now = Instant.now();

        // 1) Próxima cita = SCHEDULED
        long scheduledCount = headJobRepository.countByClient_IdUserAndStateAndScheduledTimeAfter(
                client.getIdUser(),
                HEADJobState.SCHEDULED,
                now
        );

        String nextAppointmentLabel = headJobRepository
                .findFirstByClient_IdUserAndStateAndScheduledTimeAfterOrderByScheduledTimeAsc(
                        client.getIdUser(),
                        HEADJobState.SCHEDULED,
                        now
                )
                .map(j -> formatNextAppointment(j.getScheduledTime()))
                .orElse(null);

        // 2) Recetas activas (por clientUuid)
        long activeRx = headPrescriptionRepository.countByClientUuidAndStatusIn(
                client.getUuIdUser(),
                java.util.List.of(HEADPrescriptionStatus.ISSUED)
        );

        // 3) Puntos (rápido derivado: COMPLETED * 10)
        int factor = 10;

        long completedAllTime = headJobRepository.countByClient_IdUserAndState(
                client.getIdUser(),
                HEADJobState.COMPLETED
        );
        int pointsTotal = Math.toIntExact(completedAllTime * factor);

        Instant monthStart = YearMonth.now()
                .atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        long completedThisMonth = headJobRepository.countByClient_IdUserAndStateAndCompletedAtAfter(
                client.getIdUser(),
                HEADJobState.COMPLETED,
                monthStart
        );
        int pointsDelta = Math.toIntExact(completedThisMonth * factor);

        return HEADDashboardStatsDto.builder()
                .nextAppointmentsCount((int) scheduledCount)
                .nextAppointmentLabel(nextAppointmentLabel) // "feb 22 • 14:00"
                .activePrescriptionsCount((int) activeRx)
                .prescriptionsLabel("Activas")
                .pointsTotal(pointsTotal)
                .pointsDeltaLabel("+" + pointsDelta + " este mes")
                .build();
    }

    private static String formatNextAppointment(Instant scheduledAt) {
        if (scheduledAt == null) return null;
        var zdt = scheduledAt.atZone(ZoneId.systemDefault());
        var fmt = DateTimeFormatter.ofPattern("MMM d • HH:mm", new Locale("es", "MX"));
        return fmt.format(zdt);
    }

    @Transactional(readOnly = true)
    public OptionalInt etaMinutesForProfile(
            double clientLat,
            double clientLng,
            long occupationProfileId,
            double radiusMeters
    ) {
        // usa tu mismo flujo pero filtrado por profileId y limit 1
        var dto = headShowStaffsToClientsMap.staffWithinRadiusDtoFastMulti(
                clientLat,
                clientLng,
                1,              // limit = 1 (solo el más cercano)
                true,           // useEta
                22.0,           // avgSpeedKmh (si aplica en tu cálculo)
                Set.of(occupationProfileId),
                true            // onlyAvailable
        );

        if (dto.getHeadStaffsCurrents() == null || dto.getHeadStaffsCurrents().isEmpty()) {
            return OptionalInt.empty();
        }
        var first = dto.getHeadStaffsCurrents().get(0);
        Integer eta = first.getEtaMinutes();
        return eta == null ? OptionalInt.empty() : OptionalInt.of(eta);
    }

    private String toPriceLabel(BigDecimal min) {
        if (min == null) return null;

        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US); // coma en miles
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        
        return nf.format(min);
    }

    private String etaTime(Double clientLat,
                            Double clientLng,
                            Long occupationProfileId) {
        if( clientLat != null && clientLng != null && occupationProfileId != null) {
            var eta = etaMinutesForProfile(clientLat, clientLng, occupationProfileId, 10).orElse(-1);
            return eta == -1 ? null : eta + " min";
        }
        return null;
    }

    @Transactional
    private Integer etaCurrent(Double clientLat,
                                     Double clientLng) {
        final var catalog = headOccupationsProfilesRepository.findAll();
        return catalog.stream().map(profile -> {
            return clientLat != null && clientLng != null && profile.getIdOccupationProfile() != null ? etaMinutesForProfile(clientLat,clientLng,profile.getIdOccupationProfile(), 10).orElse(0) : 0;
        }).min(Integer::compareTo).orElse(null);
    }

    public Set<String> resolvePopularPackageIds(List<HEADPackagesPersonal> packages) {
        if (packages == null || packages.isEmpty()) return Set.of();

        var packageIds = packages.stream()
                .map(HEADPackagesPersonal::getId)
                .filter(Objects::nonNull)
                .toList();

        if (packageIds.isEmpty()) return Set.of();

        var from = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);

        var rows = headJobRepository.countCompletedJobsByPackageIds(
                HEADJobState.COMPLETED,
                from,
                packageIds
        );

        if (rows == null || rows.isEmpty()) {
            return Set.of();
        }

        return rows.stream()
                .filter(Objects::nonNull)
                .filter(row -> row.getPackageId() != null)
                .filter(row -> row.getTotal() != null)
                .max(Comparator.comparingLong(row -> row.getTotal().longValue()))
                .map(row -> Set.of(row.getPackageId()))
                .orElseGet(Set::of);
    }

    public HEADPromoTags getTagsServices(Long profileId) {
        return headPromotionsMap.buildServiceTag(profileId);
    }

}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADMovePersonalToClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity.ClientFoundPayload;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity.PersonalAssignmentPayload;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.constantsServices.HEADServiceNotificationsConstants;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.request.HEADNotificationPushDataReq;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.request.HEADNotificationPushRequest;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.*;

@Service
@lombok.RequiredArgsConstructor
public class HEADActiveLocationMapService {

    private static final double AVERAGE_RADIUS_OF_EARTH_KM = 6371d;
    private static final double AVERAGE_RADIUS_OF_EARTH_METERS = 6371000d;
    public static final int kms = 10;
    public static final double mts = 10000d;

    // velocidad peatonal aprox (m/s)
    private final double speedInMetersPerSecond = 3.0;

    public HEADActiveLocationPersonal createActiveLocationPersonal(HEADWebSocketUsersEntity wsUser,
                                                                   HEADPersonalUser idPersonalUser) {
        return new HEADActiveLocationPersonal(wsUser, idPersonalUser);
    }

    public HEADWebSocketUsersEntity createActiveLocationPersonalDto(HEADActiveLocationPersonal alp) {
        var dto = new HEADWebSocketUsersEntity();
        dto.setUuIdPersonal(alp.getUuIdPersonal());
        dto.setIsActiveWork(alp.getIsActiveWork());
        dto.setIsBusy(alp.getIsBusy());
        dto.setLongitude(alp.getLongitude());
        dto.setLatitude(alp.getLatitude());
        dto.setIdSocketUser(alp.getIdSocketPersonal()); // si aquí guardas sessionId legacy, ok
        return dto;
    }

    public List<HEADWebSocketUsersEntity> activeLocationPersonalDtoList(List<HEADActiveLocationPersonal> list) {
        List<HEADWebSocketUsersEntity> out = new ArrayList<>(list.size());
        for (var alp : list) out.add(createActiveLocationPersonalDto(alp));
        return out;
    }

    // BUG FIX: el stream anterior no coleccionaba nada → devolvía lista vacía siempre
    public List<HEADWebSocketUsersEntity> nearbyActiveStaffs(List<HEADActiveLocationPersonal> all,
                                                             double userLat, double userLng) {
        return all.stream()
                .filter(x -> Boolean.TRUE.equals(x.getIsActiveWork())
                        && (x.getIsBusy() == null || !x.getIsBusy())
                        && x.getLatitude() != null && x.getLongitude() != null
                        && calculateDistanceInKilometer(userLat, userLng, x.getLatitude(), x.getLongitude()) <= kms)
                .map(this::createActiveLocationPersonalDto)
                .toList();
    }

    public HEADNotificationPushRequest notificationPushRequest(HEADWebSocketUsersEntity headPersonal) {
        var req = new HEADNotificationPushRequest();
        var data = new HEADNotificationPushDataReq();
        data.setTitle(HEADServiceNotificationsConstants.titleRequestService);
        data.setBody(HEADServiceNotificationsConstants.bodyRequestService);
        req.setTo(headPersonal.getIdSocketUser()); // si aquí ‘to’ es FCM token, reemplázalo por ese token
        req.setData(data);
        return req;
    }

    public HEADMovePersonalToClient headMovePersonalToClient(HEADServiceRequestClient src,
                                                             HEADActiveLocationPersonal alp) {
        var link = new HEADMovePersonalToClient();
        link.setIdActiveLocationPersonal(alp);
        link.setIdServiceRequestClient(src);
        return link;
    }

    public HEADWebSocketUsersEntity searchPersonalList(List<HEADActiveLocationPersonal> all,
                                                       List<HEADActivePersonal> activePersonals,
                                                       Long idUserClient,
                                                       double userLat, double userLng) {
        var candidatos = all.stream()
                .filter(x -> Boolean.TRUE.equals(x.getIsActiveWork())
                        && (x.getIsBusy() == null || !x.getIsBusy())
                        && x.getLatitude() != null && x.getLongitude() != null)
                .map(alp -> {
                    long d = calculateDistanceInMetersSafe(userLat, userLng, alp.getLatitude(), alp.getLongitude());
                    if (d > mts) return null;

                    // rechazos del día (tu lógica original compara equals a now; podrías querer truncar a fecha)
                    var rechazados = activePersonals.stream()
                            .filter(ap -> parseToDateTime(ap.getDateCurrent()).equals(LocalDateTime.now())
                                    && Boolean.TRUE.equals(ap.getIsRejected())
                                    && Objects.equals(ap.getIdPersonalUser(), alp.getIdPersonalUser()))
                            .sorted(Comparator.comparingLong(HEADActivePersonal::getIdActivePersonal).reversed())
                            .toList();

                    var rechazadoParaCliente = rechazados.stream()
                            .filter(ap -> Objects.equals(ap.getIdUserClient().getIdUser(), idUserClient))
                            .findFirst().orElse(null);

                    if (rechazados.size() < 3 && rechazadoParaCliente == null) {
                        var dto = createActiveLocationPersonalDto(alp);
                        dto.setDistanceMts(d);
                        return dto;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(HEADWebSocketUsersEntity::getDistanceMts)) // cerca primero
                .toList();

        return candidatos.stream().findFirst().orElse(null);
    }

    public PersonalAssignmentPayload createRequestForPersonal(HEADActivePersonal link,
                                                              HEADWebSocketUsersEntity selectedWs, // o HEADActiveLocationPersonal selectedALP
                                                              double clientLat, double clientLng) {
        // Si recibes HEADActiveLocationPersonal, toma su lat/lng directo.
        double personalLat = selectedWs.getLatitude();
        double personalLng = selectedWs.getLongitude();

        long distanceMts = calculateDistanceInMetersSafe(clientLat, clientLng, personalLat, personalLng);
        int etaMin = calculateTravelMetersTimeInMinutes(distanceMts);

        var p = new PersonalAssignmentPayload();
        p.setUuIdClient(link.getUuIdClient());
        p.setClientLat(clientLat);
        p.setClientLng(clientLng);
        p.setDistanceMts(distanceMts);
        p.setEtaMin(etaMin);
        p.setIdActivePersonal(link.getIdActivePersonal());
        p.setNote(null); // opcional: "cliente requiere silla de ruedas", etc.
        return p;
    }

    /** Payload que verá el cliente al tener personal asignado/visible. */
    public ClientFoundPayload createPayloadForClient(HEADWebSocketUsersEntity selectedWs,
                                                     double clientLat, double clientLng) {
        double personalLat = selectedWs.getLatitude();
        double personalLng = selectedWs.getLongitude();
        long distanceMts = calculateDistanceInMetersSafe(clientLat, clientLng, personalLat, personalLng);
        int etaMin = calculateTravelMetersTimeInMinutes(distanceMts);

        var c = new ClientFoundPayload();
        c.setUuIdPersonal(selectedWs.getUuIdPersonal());
        c.setPersonalLat(personalLat);
        c.setPersonalLng(personalLng);
        c.setDistanceMts(distanceMts);
        c.setEtaMin(etaMin);
        c.setAccepted(true); // si es sólo “disponible en radio”, pon false
        // Rellenar info opcional si la tienes:
        // c.setDisplayName(...); c.setOccupationName(...); c.setPlateOrId(...);
        return c;
    }

    private int calculateDistanceInKilometer(double userLat, double userLng,
                                             double personalLat, double personalLng) {
        double latDistance = Math.toRadians(userLat - personalLat);
        double lngDistance = Math.toRadians(userLng - personalLng);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(personalLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c);
    }

    public long calculateDistanceInMetersSafe(double userLat, double userLng, double personalLat, double personalLng) {
        double lat1 = Math.toRadians(userLat),  lon1 = Math.toRadians(userLng);
        double lat2 = Math.toRadians(personalLat), lon2 = Math.toRadians(personalLng);
        double inner = Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2 - lon1);
        inner = Math.max(-1.0, Math.min(1.0, inner));
        return Math.round(AVERAGE_RADIUS_OF_EARTH_METERS * Math.acos(inner));
    }

    public double distanceKmCeilStep(double userLat, double userLng,
                                     double personalLat, double personalLng,
                                     double stepKm) {
        long meters = calculateDistanceInMetersSafe(userLat, userLng, personalLat, personalLng);
        double km = meters / 1000.0;

        return Math.ceil(km / stepKm) * stepKm;
    }

    public Long calculateDistanceInMetersSafeInt(double userLat, double userLng,
                                                double personalLat, double personalLng) {
        long meters = calculateDistanceInMetersSafe(userLat, userLng, personalLat, personalLng);

        if (meters > Integer.MAX_VALUE) return Long.MAX_VALUE;
        if (meters < Integer.MIN_VALUE) return Long.MIN_VALUE;
        return (Long) meters;
    }



    public HEADActivePersonal headActivePersonalSaveMap(HEADClients client,
                                                        HEADClientLocationPackage req,
                                                        HEADActiveLocationPersonal alp) {
        var model = new HEADActivePersonal();
        model.setIdUserClient(client);
        model.setLatitude(req.getUserLat());
        model.setLongitude(req.getUserLong());
        model.setDateCurrent(getDateTimeCurrent());
        model.setIdPersonalUser(alp.getIdPersonalUser());
        return model;
    }

    public Integer estimateDurationSeconds(Integer distanceMeters, double avgSpeedKmh) {
        if (distanceMeters == null || distanceMeters <= 0) return null;
        if (avgSpeedKmh <= 0) avgSpeedKmh = 15.0; // fallback

        double speedMps = avgSpeedKmh * 1000.0 / 3600.0;  // km/h -> m/s
        long secs = Math.round(distanceMeters / speedMps);

        return (int) Math.max(0, secs);
    }

    public Integer durationMinutesFromSeconds(Integer durationSeconds) {
        if (durationSeconds == null) return null;
        // si quieres redondeo hacia arriba para UI tipo Uber:
        return (int) Math.ceil(durationSeconds / 60.0);
        // si quieres redondeo normal:
        // return Math.round(durationSeconds / 60f);
    }


    public int calculateTravelMetersTimeInMinutes(double distanceInMeters) {
        if (distanceInMeters <= 0) return 0;

        double seconds = distanceInMeters / speedInMetersPerSecond;
        int minutes = (int) Math.ceil(seconds / 60.0);

        return Math.max(1, minutes);
    }

    public Long calculateTravelSeconds(double distanceInMeters) {
        if (distanceInMeters <= 0) return 0L;

        return (Long) Math.round(distanceInMeters / speedInMetersPerSecond);
    }



    // asumí que tienes estos helpers en otra clase:
    private static String getDateTimeCurrent() { return LocalDateTime.now().toString(); }
    private static LocalDateTime parseToDateTime(String s) { return LocalDateTime.parse(s); }
}


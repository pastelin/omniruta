package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient.HEADClientWebSocketRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.RequestOutcome;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADErrorCommonsSocket;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Repository.HEADGeolocationRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADMovePersonalToClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADActivePersonalRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADMovePersonalToClientRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.pushNotificationsPersonal.services.IHEADPushNotificationsPersonalService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.socket.entity.SaveResult;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.Service.IHEADServiceResultApi;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.*;


@Service
@RequiredArgsConstructor
public class HEADActiveLocationPersonalService {

    // ==== Dependencias (final) → Lombok genera constructor ====
    private final HEADActiveLocationMapService headActiveLocationMapService;
    private final HEADGeolocationRepository headGeolocationRepository;
    private final HEADPersonalUserRepository personaUserRepository;
    private final HEADActivePersonalRepository headActivePersonalRepository;
    private final IHEADPushNotificationsPersonalService iheadPushNotificationsPersonalService; // @Qualifier en el field si hay varias impl.
    private final HEADClientsRepository headClientsRepository;
    private final HEADClientWebSocketRepository headClientWebSocketRepository;
    private final HEADMovePersonalToClientRepository headMovePersonalToClientRepository;
    private final HEADPackagesToProfilesRepository headPackagesToProfilesRepository;

    // Infra
    private final HEADWsEmitter emitter;          // → emite por room o por sessionId
    private final HEADPresenceStore presence;
    private final HEADStaffStateStore headStaffStateStore;// → opcional: para saber si está online

    // ==== Helpers ====
    private static boolean isRoom(String v) { return v != null && v.startsWith("user:"); }

    // ==== Casos de uso ====
    @Transactional
    public HEADActiveLocationPersonal saveActiveLocationPersonal(HEADWebSocketUsersEntity responseUser,
                                                                 HEADPersonalUser headPersonalUser) {
        var geoloc = headActiveLocationMapService.createActiveLocationPersonal(responseUser, headPersonalUser);
        return headGeolocationRepository.save(geoloc);
    }

    public List<HEADWebSocketUsersEntity> nearbyPersonalActive(double userLat, double userLng) {
        return headActiveLocationMapService.nearbyActiveStaffs(headGeolocationRepository.findAll(), userLat, userLng);
    }

    /** Re-emite actualización de ubicación del personal a los clientes mapeados dentro del radio. */
    @Transactional
    public void sendRefreshLocationToClient(HEADActiveLocationPersonal personal) {
        // Si el personal salió de servicio: limpiar mapeos y notificar REMOVEs
        if (Boolean.FALSE.equals(personal.getIsActiveWork())) {
            var movers = headMovePersonalToClientRepository
                    .findByIdActiveLocationPersonal(personal).orElse(new ArrayList<>());
            if (!movers.isEmpty()) {
                headMovePersonalToClientRepository.deleteAll(movers);
                movers.forEach(m -> {
                    String stored = m.getIdServiceRequestClient().getTokenNotification(); // hoy guarda "user:{uuid}"
                    var payload = headActiveLocationMapService.createActiveLocationPersonalDto(personal);
                    emitToStoredTarget(stored, PERSONAL_REMOVE_RESPONSE, payload);
                });
            }
            return;
        }

        // Buscar clientes en radio
        List<HEADServiceRequestClient> around;
        try (Stream<HEADServiceRequestClient> s = headClientWebSocketRepository.findServiceRequestClientRadio(
                personal.getLatitude(), personal.getLongitude(), HEADActiveLocationMapService.kms)) {
            around = s.toList();
        }

        // Limpiar mapeos viejos (para quienes estaban vinculados a este personal)
        try (Stream<HEADMovePersonalToClient> s = headMovePersonalToClientRepository
                .findByActivePersonal(
                        around.stream().map(HEADServiceRequestClient::getIdServiceRequestClient).toList(),
                        personal.getIdActivePersonal())) {

            var toDeleteIds = new ArrayList<Long>();
            s.forEach(link -> {
                String stored = link.getIdServiceRequestClient().getTokenNotification();
                var payload = headActiveLocationMapService.createActiveLocationPersonalDto(link.getIdActiveLocationPersonal());
                emitToStoredTarget(stored, PERSONAL_REMOVE_RESPONSE, payload);
                toDeleteIds.add(link.getIdStaffToClient());
            });
            if (!toDeleteIds.isEmpty()) headMovePersonalToClientRepository.deleteAllById(toDeleteIds);
        }

        // Reasignar y notificar AVAILABLE a los clientes vigentes
        around.forEach(clientCurrent -> {
            var existing = headMovePersonalToClientRepository
                    .findByServiceClient(clientCurrent.getIdServiceRequestClient(), personal.getIdActivePersonal())
                    .orElse(null);

            var newLink = headActiveLocationMapService.headMovePersonalToClient(clientCurrent, personal);
            if (existing != null) newLink.setIdStaffToClient(existing.getIdStaffToClient());
            headMovePersonalToClientRepository.save(newLink);

            String stored = clientCurrent.getTokenNotification(); // "user:{uuid}"
            var payload = headActiveLocationMapService.createActiveLocationPersonalDto(personal);
            emitToStoredTarget(stored, PERSONAL_AVAILABLE_RESPONSE, payload);
        });
    }

    /** Selecciona personal para un request del cliente y emite notificaciones. */
    public HEADErrorCommonsSocket selectedPersonalService(String userUuidClient,
                                                          HEADClientLocationPackage req) {
        var userClient = headClientsRepository.findByUuIdUser(userUuidClient).orElse(null);
        if (userClient == null) return error(400, "No eres cliente, favor de ingresar tus datos correctamente");

        var selected = headActiveLocationMapService.searchPersonalList(
                headGeolocationRepository.findAll(),
                headActivePersonalRepository.findAll(),
                userClient.getIdUser(),
                req.getUserLat(), req.getUserLong());

        if (selected == null) return error(400, "No se encontró ningún personal.");

        // Persistir vínculo y notificar:
        var result = saveClientActivePersonal(userUuidClient, selected, req);

        // Notificar al PERSONAL (por room)
        var payloadToPersonal = headActiveLocationMapService.createRequestForPersonal(result.getHeadClient(),
                selected, req.getUserLat(), req.getUserLong());
        emitter.toUser(selected.getUuIdPersonal(), IS_ACCEPTED_PERSONAL_TO_CLIENT, payloadToPersonal);

        // Notificar al CLIENTE (por room)
        var payloadToClient = headActiveLocationMapService.createPayloadForClient(selected,
                req.getUserLat(), req.getUserLong());
        emitter.toUser(userUuidClient, SEND_PERSONAL_FOUND, payloadToClient);

        return ok(200, "Se encontró al personal");
    }

    // IMPORTANTE: este NO emite por WebSocket; solo busca, persiste y arma payloads
    public RequestOutcome selectPersonalForRequest(String userUuidClient, HEADClientLocationPackage req) {
        var userClient = headClientsRepository.findByUuIdUser(userUuidClient).orElse(null);
        if (userClient == null) {
            return RequestOutcome.errorForClient(400, "No eres cliente, favor de ingresar tus datos correctamente");
        }
        // 1) elegir personal cercano
        var selectedWs = headActiveLocationMapService.searchPersonalList(
                headGeolocationRepository.findAll(),
                headActivePersonalRepository.findAll(),
                userClient.getIdUser(),
                req.getUserLat(), req.getUserLong()
        );
        if (selectedWs == null) {
            return RequestOutcome.errorForClient(404, "No se encontró personal disponible en tu zona.");
        }

        // 2) persistir vínculo cliente-personal (y room estable "user:{uuidCliente}")
        var save = saveClientActivePersonal(userUuidClient, selectedWs, req); // tu método actual devuelve info guardada

        // 3) construir payloads (para cliente y para personal)
        var payloadForClient   = headActiveLocationMapService.createPayloadForClient(
                selectedWs, req.getUserLat(), req.getUserLong());

        var payloadForPersonal = headActiveLocationMapService.createRequestForPersonal(
                save.getHeadClient(), selectedWs, req.getUserLat(), req.getUserLong());

        // 4) devolver outcome listo para que el ADAPTADOR lo emita
        return RequestOutcome.success(
                selectedWs.getUuIdPersonal(),   // targetPersonalUuid
                payloadForClient,               // clientPayload
                payloadForPersonal              // personalPayload
        );
    }


    /** Persiste elección y avisa al personal + al cliente (por room) */
    public SaveResult saveClientActivePersonal(String userUuidClient,
                                               HEADWebSocketUsersEntity wsEntity,
                                               HEADClientLocationPackage req) {
        var userClient = headClientsRepository.findByUuIdUser(userUuidClient).orElseGet(HEADClients::new);
        var selected = headGeolocationRepository.findByUuIdPersonal(wsEntity.getUuIdPersonal())
                .orElse(new HEADActiveLocationPersonal());

        var link = headActiveLocationMapService.headActivePersonalSaveMap(userClient, req, selected);
        link.setIdSocketClient("user:" + userUuidClient);

        var prev = headActivePersonalRepository.findAll().stream()
                .filter(ap -> ap.getIdUserClient().equals(userClient))
                .max(Comparator.comparingLong(HEADActivePersonal::getIdActivePersonal));

        link.setUuIdClient(prev.map(HEADActivePersonal::getUuIdClient)
                .orElseGet(HEADActiveLocationPersonalService::generatorUUID));
        link.setIsRejected(null);
        headActivePersonalRepository.save(link);

        var out = new SaveResult();
        out.setHeadClient(link);
        out.setSelected(selected);
        return out;
    }

    // ==== IHEADServiceResultApi (si sigue siendo necesario) ====
    public void onSuccess(Object objectResponse, Integer idUserClient, HEADRequestServiceClient req, Object ignore) { }
    public void onError(String errorMessage) { }

    // ==== Private ====

    private void emitToStoredTarget(String stored, String event, Object payload) {
        if (isRoom(stored)) {
            // stored = "user:{uuid}"
            var userUuid = stored.substring("user:".length());
            emitter.toUser(userUuid, event, payload);
        } else {
            // Soporte legacy: era sessionId
            emitter.toSession(stored, event, payload);
        }
    }

    private static HEADErrorCommonsSocket error(int code, String msg) {
        var e = new HEADErrorCommonsSocket();
        e.setCode(code); e.setIsSuccess(false); e.setMessage(msg);
        return e;
    }

    private static HEADErrorCommonsSocket ok(int code, String msg) {
        var r = new HEADErrorCommonsSocket();
        r.setCode(code); r.setIsSuccess(true); r.setMessage(msg);
        return r;
    }

    // Si tienes ya este helper en otra clase, usa ese:
    private static String generatorUUID() { return UUID.randomUUID().toString(); }
}

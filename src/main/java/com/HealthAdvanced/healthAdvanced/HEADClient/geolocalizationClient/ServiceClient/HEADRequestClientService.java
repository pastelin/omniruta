package com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.ServiceClient;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.Dtos.request.HEADRequestServiceClient;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.MappingClient.HEADServiceRequestClientMap;
import com.HealthAdvanced.healthAdvanced.HEADClient.geolocalizationClient.RepositoryClient.HEADClientWebSocketRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.RequestOutcome;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADErrorCommonsSocket;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationPersonalService;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils.HEADCommonsUtils.generatorUUID;

@Service("HEADRequestClientService")
@RequiredArgsConstructor
public class HEADRequestClientService implements IHEADRequestClientService {

    private final HEADClientWebSocketRepository clientWsRepo;
    private final HEADServiceRequestClientMap mapper;
    private final HEADActiveLocationPersonalService personalSvc;
    private final HEADClientsRepository clientsRepo;
    private final HEADPresenceStore presence;

    @Override
    public Object getPersonalsAvailable(HEADClientLocationPackage requestJson) {
        try {
            return mapper.headMapPersonalsAvailable(requestJson);
        } catch (JsonProcessingException e) {
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public RequestOutcome requestServiceClient(String userUuid, HEADClientLocationPackage requestJson) {
        // 1) Resolver cliente por UUID (del handshake)
        var idClientOpt = clientsRepo.findByUuIdUser(userUuid);
        if (idClientOpt.isEmpty()) {
            return RequestOutcome.errorForClient(400, "No eres cliente, favor de ingresar tus datos correctamente");
        }


        // 3) Seleccionar personal (lógica pura; persiste el vínculo y regresa decisión)
        var decision = personalSvc.selectPersonalForRequest(userUuid, requestJson);
        if (decision == null || decision.getTargetPersonalUuid() == null) {
            return RequestOutcome.errorForClient(404, "No se encontró personal disponible en tu zona.");
        }

        // 4) (Opcional) checar presencia para ajustar estrategia
        boolean online = presence.isOnline(decision.getTargetPersonalUuid());
        // Si está offline podrías encolar/push; aquí solo seguimos con el flujo normal.

        // 5) Construir payloads finales
        Object payloadForClient   = decision.getClientPayload();   // JSON para el cliente
        Object payloadForPersonal = decision.getPersonalPayload(); // JSON para el personal

        return RequestOutcome.success(
                decision.getTargetPersonalUuid(),
                payloadForClient,
                payloadForPersonal
        );
    }


    @Override
    public Map<String, Object> updateLocationClientCurrent(String userUuid, String jsonClientLocation) {
        var idClientOpt = clientsRepo.findByUuIdUser(userUuid);
        if (idClientOpt.isEmpty()) return Map.of("ok", false, "msg", "No eres cliente");

        var idClient = idClientOpt.get();
        var dto = mapper.parseToRequestClient(jsonClientLocation);

        var entityOpt = clientWsRepo.findByIdClient(idClient);
        var entity = entityOpt.orElseGet(HEADServiceRequestClient::new);

        if (entity.getIdClient() == null) {
            entity.setIdClient(idClient);
            entity.setUuIdUser(generatorUUID());
        }

        entity.setDateCurrent(LocalDateTime.now().toString());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setTokenNotification("user:" + userUuid); // room estable

        clientWsRepo.save(entity);
        return Map.of("ok", true);
    }
}

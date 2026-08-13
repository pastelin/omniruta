package com.HealthAdvanced.healthAdvanced.HEADLiveTracking.ws;

import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.dto.HEADVehicleLocationDto;
import com.HealthAdvanced.healthAdvanced.HEADLiveTracking.service.HEADTrackingLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canal WebSocket único para el demo de tracking en vivo (sin salas/rooms todavía).
 * - El "repartidor/staff" manda mensajes {"type":"location", vehicleId, lat, lng, ...}.
 * - Todos los clientes conectados (p. ej. el "cliente" viendo el mapa) reciben el broadcast.
 * Punto de partida deliberadamente simple: ver ANALISIS_PROYECTO.md, sección de plan de acción,
 * para la evolución hacia salas por servicio/job y autenticación JWT en el handshake.
 */
@Slf4j
public class HEADTrackingWebSocketHandler extends TextWebSocketHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private final HEADTrackingLocationService locationService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public HEADTrackingWebSocketHandler(HEADTrackingLocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("[tracking-ws] conectado {} (total={})", session.getId(), sessions.size());
        sendSnapshot(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("[tracking-ws] desconectado {} (total={})", session.getId(), sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            var node = MAPPER.readTree(message.getPayload());
            String type = node.hasNonNull("type") ? node.get("type").asText() : "location";

            if (!"location".equals(type)) {
                log.warn("[tracking-ws] tipo de mensaje no soportado: {}", type);
                return;
            }

            var dto = MAPPER.treeToValue(node, HEADVehicleLocationDto.class);
            broadcastLocation(locationService.save(dto));
        } catch (Exception e) {
            log.warn("[tracking-ws] mensaje inválido de {}: {}", session.getId(), e.toString());
        }
    }

    /** Reutilizado por el REST controller para difundir una actualización recibida por HTTP. */
    public void broadcastLocation(HEADVehicleLocationDto dto) {
        broadcast(Map.of("type", "location", "vehicle", dto));
    }

    private void sendSnapshot(WebSocketSession session) {
        var snapshot = Map.of("type", "snapshot", "vehicles", locationService.listActive());
        sendTo(session, snapshot);
    }

    private void broadcast(Object payload) {
        String json = writeJson(payload);
        if (json == null) return;
        sessions.values().forEach(s -> sendRaw(s, json));
    }

    private void sendTo(WebSocketSession session, Object payload) {
        String json = writeJson(payload);
        if (json != null) sendRaw(session, json);
    }

    private void sendRaw(WebSocketSession session, String json) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.warn("[tracking-ws] no se pudo enviar a {}: {}", session.getId(), e.toString());
        }
    }

    private String writeJson(Object payload) {
        try {
            return MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("[tracking-ws] error serializando payload: {}", e.toString());
            return null;
        }
    }
}

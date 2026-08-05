package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.implementations;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.HEADWSRequest;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.util.TriConsumer;

public class HEADListenersSocket {

    private final SocketIONamespace ns;
    private final ObjectMapper mapper;

    public HEADListenersSocket(SocketIONamespace ns, ObjectMapper mapper) {
        this.ns = ns;
        this.mapper = mapper;
    }

    public <T> void onTx(
            String event,
            Class<T> txClass,
            TriConsumer<SocketIOClient, T, AckRequest> handler
    ) {
        ns.addEventListener(event, HEADWSRequest.class, (client, rawReq, ack) -> {
            try {
                @SuppressWarnings("unchecked")
                HEADWSRequest<Object> req = (HEADWSRequest<Object>) rawReq;

                T tx = mapper.convertValue(req.transaction(), txClass);
                handler.accept(client, tx, ack);

            } catch (Exception e) {
                if (ack.isAckRequested()) {
                    ack.sendAckData("bad_request", e.getMessage());
                }
            }
        });
    }

    public <T> void onTx(
            String event,
            TypeReference<T> txType,
            TriConsumer<SocketIOClient, T, AckRequest> handler
    ) {
        ns.addEventListener(event, HEADWSRequest.class, (client, rawReq, ack) -> {
            try {
                @SuppressWarnings("unchecked")
                HEADWSRequest<Object> req = (HEADWSRequest<Object>) rawReq;

                T tx = mapper.convertValue(req.transaction(), txType);
                handler.accept(client, tx, ack);

            } catch (Exception e) {
                if (ack.isAckRequested()) {
                    ack.sendAckData("bad_request", e.getMessage());
                }
            }
        });
    }
}

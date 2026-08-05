package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADClientUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos.HEADOfferDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.corundumstudio.socketio.SocketIOClient;

public interface HEADWsEmitter {
    void toUser(String userUuid, String event, Object payload);
    void toSession(String sessionId, String event, Object payload); // opcional
    void addClientToJob(SocketIOClient c, Long jobId);
    /**
     * Envía la notificación de una nueva oferta de trabajo al conductor específico.
     * @param staffUuid UUID del conductor.
     * @param payload El DTO de la oferta (OfferDto).
     */
    void emitOffer(String staffUuid, HEADOfferDto payload);
    void emitToClient(String userUuid, String event, Object payload);
    void emitJobAcceptedToStaff(String uuIdUser, Long jobId);
}

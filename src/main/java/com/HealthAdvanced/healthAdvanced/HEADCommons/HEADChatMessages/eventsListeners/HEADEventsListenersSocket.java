package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsListeners;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.HEADClientStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADAckResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client.entity.HEADWsEnvelope;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADChatPresenceUpdateSocketDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.entityDto.HEADLocUpdateDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service.HEADChatService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADChatSendMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.*;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import kotlin.reflect.KClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import static kotlin.jvm.internal.Reflection.typeOf;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADEventsListenersSocket {

    private final HEADPresenceStore presence;
    private final HEADChatService chatService;
    private final HEADClientStateStore clientState;
    private final HEADStaffStateStore staffState;

    /**
     * Helper para obtener el uuid del usuario desde el socket.
     * Asume que en onConnect ya hiciste algo como: client.set("userUuid", uuIdUser);
     */
    private static String uuidOf(SocketIOClient c) {
        return c.get("userUuid");
    }

    // ------------------------------------------------------------------------
    // 1) Enviar mensaje: CHAT_SEND_MESSAGE
    // ------------------------------------------------------------------------

    public void onSendMessage(SocketIOClient client,
                              HEADChatSendMessageRequest req,
                              AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(senderUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                HEADChatMessageDto dto = chatService.sendMessage(req, senderUuid);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok("MSG_SENT", dto));
                }
            } catch (Exception ex) {
                log.error("[onSendMessage] error senderUuid={} err={}", senderUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("MSG_SEND_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onSendMessage]"));
    }

    // ------------------------------------------------------------------------
    // 2) Marcar entregados: CHAT_MARK_DELIVERED
    // ------------------------------------------------------------------------

    public void onMarkDelivered(SocketIOClient client,
                                HEADChatMarkDeliveredRequest req,
                                AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(recipientUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                HEADChatMessageStatusUpdateDto update =
                        chatService.markAsDelivered(req.conversationId(), recipientUuid);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok("DELIVERED_OK", update));
                }
            } catch (Exception ex) {
                log.error("[onMarkDelivered] error recipientUuid={} err={}", recipientUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("DELIVERED_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onMarkDelivered]"));
    }

    // ------------------------------------------------------------------------
    // 3) Marcar leídos: CHAT_MARK_READ
    // ------------------------------------------------------------------------

    public void onMarkRead(SocketIOClient client,
                           HEADChatMarkReadRequest req,
                           AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(recipientUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                HEADChatMessageStatusUpdateDto update =
                        chatService.markAsRead(req.conversationId(), recipientUuid);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok("READ_OK", update));
                }
            } catch (Exception ex) {
                log.error("[onMarkRead] error recipientUuid={} err={}", recipientUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("READ_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onMarkRead]"));
    }

    // ------------------------------------------------------------------------
    // 4) Historial de conversación: CHAT_HISTORY_REQUEST
    // ------------------------------------------------------------------------

    public void onHistory(SocketIOClient client,
                          HEADChatHistoryRequest req,
                          AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(userUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                chatService.getConversationHistory(userUuid,req);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok("HISTORY_OK", HEADAckResponse.oks()));
                }
            } catch (Exception ex) {
                log.error("[onHistory] error userUuid={} err={}", userUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("HISTORY_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onHistory]"));
    }

    // ------------------------------------------------------------------------
    // 5) Resumen de no leídos: CHAT_UNREAD_SUMMARY_REQ
    // ------------------------------------------------------------------------

    public void onUnreadSummary(SocketIOClient client,
                                Boolean isRead,
                                AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(userUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                chatService.pushUnreadSummary(userUuid);

                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.ok("UNREAD_SUMMARY_OK", HEADAckResponse.oks()));
                }
            } catch (Exception ex) {
                log.error("[onUnreadSummary] error userUuid={} err={}", userUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("UNREAD_SUMMARY_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onUnreadSummary]"));
    }

    // ------------------------------------------------------------------------
    // 6) Typing (escribiendo...): CHAT_TYPING
    // ------------------------------------------------------------------------

    public void onTyping(SocketIOClient client,
                         HEADChatTypingRequest req,
                         AckRequest ack) {

        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(fromUuid -> {
            try {
                presence.renew(client.getSessionId().toString());

                // El service arma el DTO y publica a través del publisher
                chatService.handleTyping(req, fromUuid);

                if (ack.isAckRequested()) {
                    // Si quieres no devolver nada, puedes mandar result=null
                    ack.sendAckData(HEADWsEnvelope.ok("TYPING_OK",HEADAckResponse.oks() ));
                }
            } catch (Exception ex) {
                log.error("[onTyping] error fromUuid={} err={}", fromUuid, ex.toString(), ex);
                if (ack.isAckRequested()) {
                    ack.sendAckData(HEADWsEnvelope.fail("TYPING_ERROR: " + ex.getMessage()));
                }
            }
        }, () -> handleNoAuth(client, ack, "[onTyping]"));
    }

    public void onActiveUserChat(SocketIOClient client,HEADActiveUserChayRequest headActiveUserChayRequest, AckRequest ack) {
        Optional.ofNullable(uuidOf(client)).ifPresentOrElse(userUuid -> {
            presence.renew(client.getSessionId().toString());
            chatService.renew(userUuid);
            chatService.activeUser(userUuid, headActiveUserChayRequest.conversationId());
        }, () -> handleNoAuth(client, ack, "[onActiveUser]"));
    }

    public void onInactiveUser(SocketIOClient client, HEADChatActiveConversationClearRequest headChatActiveConversationClearRequest, AckRequest ack) {
        Optional.ofNullable(uuidOf(client)).ifPresentOrElse( userUuid -> {
            presence.renew(client.getSessionId().toString());
                chatService.inactiveUserCaller(userUuid, headChatActiveConversationClearRequest.conversationId());
        }, () -> handleNoAuth(client,ack,"[onInactiveUser]"));
    }

    public void onPresenceUpdate(SocketIOClient c, HEADChatPresenceDto dto, AckRequest ack) {

        java.util.Optional.ofNullable(uuidOf(c))
                .ifPresentOrElse(userUuid -> {
                    presence.renew(c.getSessionId().toString());
                    chatService.renew(userUuid);
                    var userType = chatService.detectType(userUuid); // CLIENT / STAFF (sácalo del JWT del socket)
                    var sid = c.getSessionId().toString();

                    // 1) online por tener sesión conectada

                    // 2) appActive/currentJobId viene del front (foreground/background + chat abierto)
                    boolean appActive = dto != null && dto.appActive();
                    Long currentJobId = dto != null ? dto.currentJobId() : null;

                    // 3) guardar estado según tipo
                    switch (userType) {
                        case CLIENT -> clientState.setAppActive(userUuid, appActive);
                        case STAFF  -> staffState.setAppActive(userUuid, appActive);
                        default     -> { /* ignore */ }
                    }

                }, () -> handleNoAuth(c, ack, "[onPresenceUpdate]"));
    }



    // ------------------------------------------------------------------------
    // Helper común para cuando no hay uuid (no autenticado)
    // ------------------------------------------------------------------------

    private void handleNoAuth(SocketIOClient client, AckRequest ack, String ctx) {
        log.warn("{} unauthenticated socket sessionId={}", ctx, client.getSessionId());
        if (ack.isAckRequested()) {
            ack.sendAckData(HEADWsEnvelope.fail("UNAUTHORIZED"));
        }
        client.disconnect();
    }
}


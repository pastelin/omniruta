package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.socket;



import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;

import java.util.Optional;


public interface HEADCallRoutingStore {

    record Participant(String uuid, HEADChatParticipantType type) {}

    void bind(String callId, Participant a, Participant b);

    boolean isParticipant(String callId, String uuid);

    Optional<Participant> other(String callId, String uuid);

    Optional<Participant> getA(String callId);
    Optional<Participant> getB(String callId);

    void remove(String callId);

    /** Renueva TTL para que no expire una llamada activa. */
    default void touch(String callId) {}

    /** (Opcional) para cortar llamada al desconectar */
    default Optional<String> activeCallOf(String userUuid) { return Optional.empty(); }
}
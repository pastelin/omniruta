package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.socket;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HEADStripeAccountSocketEmitter {

    private final HEADWsEmitter emitter;

    public void emitToStaff(String staffUuid, HEADStripeAccountUiResponse payload) {
        emitter.toUser(
                staffUuid,
                HEADWsEventsStripe.STAFF_STRIPE_ACCOUNT_UPDATED,
                payload
        );
    }
}

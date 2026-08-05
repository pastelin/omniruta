package com.HealthAdvanced.healthAdvanced.HEADFinance.application.stripe;

import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.mapper.HEADStripeAccountUiMapper;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.socket.HEADStripeAccountSocketEmitter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class HEADStripeAccountRealtimeService {

    private final HEADStripeAccountUiMapper stripeAccountUiMapper;
    private final HEADStripeAccountSocketEmitter stripeAccountSocketEmitter;

    public void emitAfterCommit(HEADStaffToStripeAccount rel) {
        HEADStripeAccountUiResponse payload =
                stripeAccountUiMapper.toResponse(rel, null, null);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    stripeAccountSocketEmitter.emitToStaff(
                            rel.getStaffUser().getUidUser(),
                            payload
                    );
                }
            });
        } else {
            stripeAccountSocketEmitter.emitToStaff(
                    rel.getStaffUser().getUidUser(),
                    payload
            );
        }
    }

    public void emitAfterCommit(
            HEADStaffToStripeAccount rel,
            String bankName,
            String bankLast4
    ) {
        HEADStripeAccountUiResponse payload =
                stripeAccountUiMapper.toResponse(rel, bankName, bankLast4);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    stripeAccountSocketEmitter.emitToStaff(
                            rel.getStaffUser().getUidUser(),
                            payload
                    );
                }
            });
        } else {
            stripeAccountSocketEmitter.emitToStaff(
                    rel.getStaffUser().getUidUser(),
                    payload
            );
        }
    }
}
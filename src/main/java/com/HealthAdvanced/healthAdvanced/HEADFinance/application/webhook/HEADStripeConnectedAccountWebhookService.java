package com.HealthAdvanced.healthAdvanced.HEADFinance.application.webhook;

import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.mapper.HEADStripeAccountUiMapper;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.socket.HEADStripeAccountSocketEmitter;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class HEADStripeConnectedAccountWebhookService {

    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;
    private final HEADStripeAccountUiMapper stripeAccountUiMapper;
    private final HEADStripeAccountSocketEmitter stripeAccountSocketEmitter;

    @Transactional
    public void handle(Event event) {
        String type = event.getType();
        if (type == null) return;

        switch (type) {
            case "account.updated" -> handleAccountUpdated(event);
            case "account.external_account.created",
                 "account.external_account.updated",
                 "account.external_account.deleted" -> handleExternalAccountChanged(event);
            case "person.updated" -> handlePersonUpdated(event);
            case "account.application.deauthorized" -> handleDeauthorized(event);
            default -> { }
        }
    }

    private void handleAccountUpdated(Event event) {
        Object stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (!(stripeObject instanceof Account account)) return;

        staffToStripeAccountRepository.findByConnectedAccountId(account.getId())
                .ifPresent(rel -> {
                    rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
                    rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
                    rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
                    rel.setStripeStatus(resolveStatus(account));

                    HEADStaffToStripeAccount saved = staffToStripeAccountRepository.save(rel);

                    HEADStripeAccountUiResponse payload =
                            stripeAccountUiMapper.toResponse(saved, null, null);

                    emitAfterCommit(saved, payload);
                });
    }

    private void handleExternalAccountChanged(Event event) {
        String connectedAccountId = event.getAccount();
        if (connectedAccountId == null || connectedAccountId.isBlank()) return;

        staffToStripeAccountRepository.findByConnectedAccountId(connectedAccountId)
                .ifPresent(rel -> {
                    try {
                        Account account = Account.retrieve(connectedAccountId);

                        rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
                        rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
                        rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
                        rel.setStripeStatus(resolveStatus(account));

                        HEADStaffToStripeAccount saved = staffToStripeAccountRepository.save(rel);

                        HEADStripeAccountUiResponse payload =
                                stripeAccountUiMapper.toResponse(saved, null, null);

                        emitAfterCommit(saved, payload);
                    } catch (StripeException ignored) {
                    }
                });
    }

    private void handlePersonUpdated(Event event) {
        String connectedAccountId = event.getAccount();
        if (connectedAccountId == null || connectedAccountId.isBlank()) return;

        staffToStripeAccountRepository.findByConnectedAccountId(connectedAccountId)
                .ifPresent(rel -> {
                    try {
                        Account account = Account.retrieve(connectedAccountId);

                        rel.setDetailsSubmitted(Boolean.TRUE.equals(account.getDetailsSubmitted()));
                        rel.setPayoutsEnabled(Boolean.TRUE.equals(account.getPayoutsEnabled()));
                        rel.setChargesEnabled(Boolean.TRUE.equals(account.getChargesEnabled()));
                        rel.setStripeStatus(resolveStatus(account));

                        HEADStaffToStripeAccount saved = staffToStripeAccountRepository.save(rel);

                        HEADStripeAccountUiResponse payload =
                                stripeAccountUiMapper.toResponse(saved, null, null);

                        emitAfterCommit(saved, payload);
                    } catch (StripeException ignored) {
                    }
                });
    }

    private void handleDeauthorized(Event event) {
        String connectedAccountId = event.getAccount();
        if (connectedAccountId == null || connectedAccountId.isBlank()) return;

        staffToStripeAccountRepository.findByConnectedAccountId(connectedAccountId)
                .ifPresent(rel -> {
                    rel.setDetailsSubmitted(false);
                    rel.setPayoutsEnabled(false);
                    rel.setChargesEnabled(false);
                    rel.setStripeStatus(HEADStripeAccountStatus.ERROR);

                    HEADStaffToStripeAccount saved = staffToStripeAccountRepository.save(rel);

                    HEADStripeAccountUiResponse payload =
                            stripeAccountUiMapper.toResponse(saved, null, null);

                    emitAfterCommit(saved, payload);
                });
    }

    private void emitAfterCommit(
            HEADStaffToStripeAccount rel,
            HEADStripeAccountUiResponse payload
    ) {
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

    private HEADStripeAccountStatus resolveStatus(Account account) {
        boolean detailsSubmitted = Boolean.TRUE.equals(account.getDetailsSubmitted());
        boolean payoutsEnabled = Boolean.TRUE.equals(account.getPayoutsEnabled());

        var requirements = account.getRequirements();

        boolean hasCurrentlyDue = requirements != null
                && requirements.getCurrentlyDue() != null
                && !requirements.getCurrentlyDue().isEmpty();

        boolean hasPastDue = requirements != null
                && requirements.getPastDue() != null
                && !requirements.getPastDue().isEmpty();

        boolean hasPendingVerification = requirements != null
                && requirements.getPendingVerification() != null
                && !requirements.getPendingVerification().isEmpty();

        String disabledReason = requirements != null ? requirements.getDisabledReason() : null;

        if (!detailsSubmitted) return HEADStripeAccountStatus.ONBOARDING;
        if (disabledReason != null && !disabledReason.isBlank()) return HEADStripeAccountStatus.ERROR;
        if (hasPastDue) return HEADStripeAccountStatus.ERROR;
        if (payoutsEnabled) return HEADStripeAccountStatus.VERIFIED;
        if (hasPendingVerification || hasCurrentlyDue) return HEADStripeAccountStatus.PENDING;

        return HEADStripeAccountStatus.PENDING;
    }
}
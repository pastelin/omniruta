package com.HealthAdvanced.healthAdvanced.HEADFinance.domain.mapper;

import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStripeAccountUiResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStripeAccountUiState;
import org.springframework.stereotype.Component;

@Component
public class HEADStripeAccountUiMapper {

    public HEADStripeAccountUiResponse empty() {
        return new HEADStripeAccountUiResponse(
                null,
                HEADStripeAccountUiState.EMPTY,
                "Agrega tu cuenta de depósito",
                "Conecta tu cuenta con Stripe para recibir tus retiros.",
                false,
                false,
                false,
                false,
                null,
                null,
                null,
                true,
                "Agregar cuenta",
                "START_ONBOARDING",
                false,
                null,
                null
        );
    }

    public HEADStripeAccountUiResponse toResponse(
            HEADStaffToStripeAccount rel,
            String bankName,
            String bankLast4
    ) {
        if (rel == null || rel.getConnectedAccountId() == null || rel.getConnectedAccountId().isBlank()) {
            return empty();
        }

        HEADStripeAccountUiState uiState = mapUiState(rel);

        return switch (uiState) {
            case EMPTY -> empty();

            case LOADING -> new HEADStripeAccountUiResponse(
                    rel.getConnectedAccountId(),
                    HEADStripeAccountUiState.LOADING,
                    "Conectando con Stripe",
                    "Estamos validando el estado de tu cuenta.",
                    bool(rel.getDetailsSubmitted()),
                    bool(rel.getPayoutsEnabled()),
                    bool(rel.getChargesEnabled()),
                    hasPayoutMethod(rel),
                    payoutMethodType(rel),
                    bankName,
                    bankLast4,
                    false,
                    null,
                    null,
                    false,
                    null,
                    null
            );

            case PENDING -> new HEADStripeAccountUiResponse(
                    rel.getConnectedAccountId(),
                    HEADStripeAccountUiState.PENDING,
                    "Tu cuenta está en revisión",
                    "Stripe está validando tu información.",
                    bool(rel.getDetailsSubmitted()),
                    bool(rel.getPayoutsEnabled()),
                    bool(rel.getChargesEnabled()),
                    hasPayoutMethod(rel),
                    payoutMethodType(rel),
                    bankName,
                    bankLast4,
                    true,
                    "Ver estado",
                    "CHECK_STATUS",
                    false,
                    null,
                    null
            );

            case VERIFIED -> new HEADStripeAccountUiResponse(
                    rel.getConnectedAccountId(),
                    HEADStripeAccountUiState.VERIFIED,
                    "Cuenta verificada",
                    "Tu cuenta ya está lista para recibir depósitos.",
                    bool(rel.getDetailsSubmitted()),
                    bool(rel.getPayoutsEnabled()),
                    bool(rel.getChargesEnabled()),
                    hasPayoutMethod(rel),
                    payoutMethodType(rel),
                    bankName,
                    bankLast4,
                    true,
                    "Ver cuenta",
                    "VIEW_ACCOUNT",
                    false,
                    null,
                    null
            );

            case ERROR -> new HEADStripeAccountUiResponse(
                    rel.getConnectedAccountId(),
                    HEADStripeAccountUiState.ERROR,
                    "Hubo un problema con tu cuenta",
                    "Necesitas actualizar tu información en Stripe para continuar.",
                    bool(rel.getDetailsSubmitted()),
                    bool(rel.getPayoutsEnabled()),
                    bool(rel.getChargesEnabled()),
                    hasPayoutMethod(rel),
                    payoutMethodType(rel),
                    bankName,
                    bankLast4,
                    true,
                    "Corregir cuenta",
                    "RESUME_ONBOARDING",
                    false,
                    null,
                    null
            );

            case CANCELED -> new HEADStripeAccountUiResponse(
                    rel.getConnectedAccountId(),
                    HEADStripeAccountUiState.CANCELED,
                    "Proceso cancelado",
                    "No terminaste la conexión con Stripe.",
                    bool(rel.getDetailsSubmitted()),
                    bool(rel.getPayoutsEnabled()),
                    bool(rel.getChargesEnabled()),
                    hasPayoutMethod(rel),
                    payoutMethodType(rel),
                    bankName,
                    bankLast4,
                    true,
                    "Intentar de nuevo",
                    "START_ONBOARDING",
                    false,
                    null,
                    null
            );
        };
    }

    private HEADStripeAccountUiState mapUiState(HEADStaffToStripeAccount rel) {
        if (rel.getStripeStatus() == null) {
            return HEADStripeAccountUiState.LOADING;
        }

        return switch (rel.getStripeStatus()) {
            case EMPTY -> HEADStripeAccountUiState.EMPTY;
            case ONBOARDING -> HEADStripeAccountUiState.LOADING;
            case PENDING -> HEADStripeAccountUiState.PENDING;
            case VERIFIED -> HEADStripeAccountUiState.VERIFIED;
            case ERROR -> HEADStripeAccountUiState.ERROR;
            case CANCELED -> HEADStripeAccountUiState.CANCELED;
        };
    }

    private boolean hasPayoutMethod(HEADStaffToStripeAccount rel) {
        return rel.getDefaultExternalAccountId() != null
                && !rel.getDefaultExternalAccountId().isBlank();
    }

    private String payoutMethodType(HEADStaffToStripeAccount rel) {
        return rel.getDefaultExternalAccountType() != null
                ? rel.getDefaultExternalAccountType().name()
                : null;
    }

    private boolean bool(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
package com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayout;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayoutItem;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffToStripeAccount;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutItemRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.IHEADStaffToStripeAccountRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.model.Transfer;
import com.stripe.net.RequestOptions;
import com.stripe.param.PayoutCreateParams;
import com.stripe.param.TransferCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
public class HEADExecuteStaffPayoutService {

    private final HEADStaffPayoutRepository payoutRepository;
    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;

    @Transactional
    public HEADStaffPayoutResponse execute(Long payoutId) {
        HEADStaffPayout payout = payoutRepository.findByIdForUpdate(payoutId)
                .orElseThrow(() -> new HEADBadRequestException("Payout no encontrado"));

        if (payout.getStatus() != HEADStaffPayoutStatus.APPROVED) {
            throw new HEADBadRequestException("El payout debe estar APPROVED para ejecutarse");
        }

        if (payout.getFinalPayoutAmount() == null || payout.getFinalPayoutAmount().signum() <= 0) {
            throw new HEADBadRequestException("El payout no tiene monto válido");
        }

        HEADStaffToStripeAccount stripeRel = staffToStripeAccountRepository
                .findByStaffUser(payout.getStaffUser())
                .orElseThrow(() -> new HEADBadRequestException("El staff no tiene cuenta conectada en Stripe"));

        if (stripeRel.getConnectedAccountId() == null || stripeRel.getConnectedAccountId().isBlank()) {
            throw new HEADBadRequestException("El staff no tiene connectedAccountId");
        }

        if (Boolean.FALSE.equals(stripeRel.getPayoutsEnabled())) {
            throw new HEADBadRequestException("La cuenta del staff no tiene payouts habilitados");
        }

        payout.setStatus(HEADStaffPayoutStatus.PROCESSING);
        payoutRepository.save(payout);

        try {
            String transferId = createTransfer(
                    payout.getFinalPayoutAmount(),
                    payout.getCurrency(),
                    stripeRel.getConnectedAccountId(),
                    payout.getId(),
                    payout.getStaffUser().getIdUser(),
                    payout.getPeriodType().name()
            );

            Payout stripePayout = createManualPayout(
                    payout.getFinalPayoutAmount(),
                    payout.getCurrency(),
                    stripeRel.getConnectedAccountId()
            );

            payout.setExternalTransferId(transferId);
            payout.setExternalPayoutId(stripePayout.getId());
            payout.setStripeConnectedAccountId(stripeRel.getConnectedAccountId());
            payout.setExternalPayoutStatus(stripePayout.getStatus());
            payout.setFailureCode(null);
            payout.setFailureMessage(null);
            payout.setNote("Payout enviado a Stripe, pendiente de confirmación");
            payoutRepository.save(payout);

            return toResponse(payout);

        } catch (Exception e) {
            payout.setStatus(HEADStaffPayoutStatus.FAILED);
            payout.setFailureMessage(buildFailureMessage(e));
            payout.setNote(buildFailureNote(e));
            payoutRepository.save(payout);

            throw new HEADBadRequestException("No se pudo ejecutar el payout: " + e.getMessage());
        }
    }

    private String createTransfer(
            BigDecimal amount,
            String currency,
            String connectedAccountId,
            Long payoutId,
            Long staffUserId,
            String periodType
    ) throws StripeException {
        TransferCreateParams params = TransferCreateParams.builder()
                .setAmount(toMinorUnits(amount))
                .setCurrency(currency.toLowerCase())
                .setDestination(connectedAccountId)
                .putMetadata("payoutId", String.valueOf(payoutId))
                .putMetadata("staffUserId", String.valueOf(staffUserId))
                .putMetadata("periodType", periodType)
                .build();

        return Transfer.create(params).getId();
    }

    private Payout createManualPayout(
            BigDecimal amount,
            String currency,
            String connectedAccountId
    ) throws StripeException {
        RequestOptions requestOptions = RequestOptions.builder()
                .setStripeAccount(connectedAccountId)
                .build();

        PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(toMinorUnits(amount))
                .setCurrency(currency.toLowerCase())
                .build();

        return Payout.create(params, requestOptions);
    }

    private Long toMinorUnits(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
    }

    private String buildFailureNote(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().trim() : "Error desconocido";
        String note = "Payout fallido: " + message;
        return note.length() > 255 ? note.substring(0, 255) : note;
    }

    private String buildFailureMessage(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().trim() : "Error desconocido";
        return message.length() > 255 ? message.substring(0, 255) : message;
    }

    private HEADStaffPayoutResponse toResponse(HEADStaffPayout payout) {
        return new HEADStaffPayoutResponse(
                payout.getId(),
                payout.getStatus().name(),
                payout.getFinalPayoutAmount(),
                payout.getCurrency(),
                payout.getItemCount(),
                payout.getRequestedAt(),
                payout.getPaidAt()
        );
    }
}
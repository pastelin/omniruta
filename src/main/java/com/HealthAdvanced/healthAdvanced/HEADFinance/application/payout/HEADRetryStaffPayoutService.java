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
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADRetryStaffPayoutService {

    private final HEADStaffPayoutRepository payoutRepository;
    private final HEADStaffPayoutItemRepository payoutItemRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final IHEADStaffToStripeAccountRepository staffToStripeAccountRepository;

    @Transactional
    public HEADStaffPayoutResponse execute(Long payoutId) {
        HEADStaffPayout payout = payoutRepository.findByIdForUpdate(payoutId)
                .orElseThrow(() -> new HEADBadRequestException("Payout no encontrado"));

        if (payout.getStatus() != HEADStaffPayoutStatus.FAILED) {
            throw new HEADBadRequestException("Solo se puede reintentar un payout en estado FAILED");
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

        validateReservedJobs(payout.getId());

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

            String stripePayoutId = createManualPayout(
                    payout.getFinalPayoutAmount(),
                    payout.getCurrency(),
                    stripeRel.getConnectedAccountId()
            );

            markJobsAsPaid(payout.getId());

            payout.setExternalTransferId(transferId);
            payout.setNote("retry stripePayoutId=" + stripePayoutId);
            payout.setPaidAt(Instant.now());
            payout.setStatus(HEADStaffPayoutStatus.PAID);

            payoutRepository.save(payout);

            return toResponse(payout);

        } catch (Exception e) {
            payout.setStatus(HEADStaffPayoutStatus.FAILED);
            payout.setNote(buildFailureNote(e));
            payoutRepository.save(payout);

            throw new HEADBadRequestException("No se pudo reintentar el payout: " + e.getMessage());
        }
    }

    private void validateReservedJobs(Long payoutId) {
        List<HEADStaffPayoutItem> items = payoutItemRepository.findByPayout_Id(payoutId);

        items.forEach(item -> {
            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(item.getJob().getId())
                    .orElseThrow(() -> new HEADBadRequestException(
                            "Snapshot financiero no encontrado para job=" + item.getJob().getId()
                    ));

            if (financial.getPayoutStatus() != HEADJobPayoutStatus.RESERVED) {
                throw new HEADBadRequestException(
                        "El job no está RESERVED para retry. jobId=" + financial.getJobId()
                );
            }
        });
    }

    private void markJobsAsPaid(Long payoutId) {
        List<HEADStaffPayoutItem> items = payoutItemRepository.findByPayout_Id(payoutId);

        items.forEach(item -> {
            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(item.getJob().getId())
                    .orElseThrow(() -> new HEADBadRequestException(
                            "Snapshot financiero no encontrado para job=" + item.getJob().getId()
                    ));

            financial.setPayoutStatus(HEADJobPayoutStatus.PAID);
        });
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
                .putMetadata("mode", "retry")
                .build();

        Transfer transfer = Transfer.create(params);
        return transfer.getId();
    }

    private String createManualPayout(
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

        Payout payout = Payout.create(params, requestOptions);
        return payout.getId();
    }

    private Long toMinorUnits(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
    }

    private String buildFailureNote(Exception e) {
        String message = e.getMessage() != null ? e.getMessage().trim() : "Error desconocido";
        String note = "Retry fallido: " + message;
        return note.length() > 255 ? note.substring(0, 255) : note;
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
package com.HealthAdvanced.healthAdvanced.HEADFinance.application.webhook;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayout;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayoutItem;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutItemRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutRepository;
import com.stripe.model.Event;
import com.stripe.model.Payout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HEADStripePayoutWebhookService {

    private final HEADStaffPayoutRepository payoutRepository;
    private final HEADStaffPayoutItemRepository payoutItemRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;

    @Transactional
    public void handle(Event event) {
        String type = event.getType();

        if (!supports(type)) return;

        Object stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElse(null);

        if (!(stripeObject instanceof Payout stripePayout)) return;

        HEADStaffPayout payout = payoutRepository.findByExternalPayoutId(stripePayout.getId())
                .orElse(null);

        if (payout == null) return;

        switch (type) {
            case "payout.updated" -> handleUpdated(payout, stripePayout);
            case "payout.paid" -> handlePaid(payout, stripePayout);
            case "payout.failed" -> handleFailed(payout, stripePayout);
            default -> { }
        }
    }

    private boolean supports(String type) {
        return "payout.updated".equals(type)
                || "payout.paid".equals(type)
                || "payout.failed".equals(type);
    }

    private void handleUpdated(HEADStaffPayout payout, Payout stripePayout) {
        payout.setExternalPayoutStatus(stripePayout.getStatus());
        payoutRepository.save(payout);
    }

    private void handlePaid(HEADStaffPayout payout, Payout stripePayout) {
        payout.setExternalPayoutStatus(stripePayout.getStatus());
        payout.setFailureCode(null);
        payout.setFailureMessage(null);
        payout.setPaidAt(Instant.now());
        payout.setStatus(HEADStaffPayoutStatus.PAID);
        payoutRepository.save(payout);

        markReservedJobsAsPaid(payout.getId());
    }

    private void handleFailed(HEADStaffPayout payout, Payout stripePayout) {
        payout.setExternalPayoutStatus(stripePayout.getStatus());
        payout.setFailureCode(stripePayout.getFailureCode());
        payout.setFailureMessage(buildFailureMessage(stripePayout));
        payout.setStatus(HEADStaffPayoutStatus.FAILED);
        payoutRepository.save(payout);

        releaseReservedJobs(payout.getId());
    }

    private void markReservedJobsAsPaid(Long payoutId) {
        List<HEADStaffPayoutItem> items = payoutItemRepository.findByPayout_Id(payoutId);

        items.forEach(item -> {
            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(item.getJob().getId())
                    .orElseThrow(() -> new HEADBadRequestException(
                            "Snapshot financiero no encontrado para job=" + item.getJob().getId()
                    ));

            if (financial.getPayoutStatus() == HEADJobPayoutStatus.RESERVED) {
                financial.setPayoutStatus(HEADJobPayoutStatus.PAID);
            }
        });
    }

    private void releaseReservedJobs(Long payoutId) {
        List<HEADStaffPayoutItem> items = payoutItemRepository.findByPayout_Id(payoutId);

        items.forEach(item -> {
            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(item.getJob().getId())
                    .orElseThrow(() -> new HEADBadRequestException(
                            "Snapshot financiero no encontrado para job=" + item.getJob().getId()
                    ));

            if (financial.getPayoutStatus() == HEADJobPayoutStatus.RESERVED) {
                financial.setPayoutStatus(HEADJobPayoutStatus.AVAILABLE);
            }
        });
    }

    private String buildFailureMessage(Payout payout) {
        String message = payout.getFailureCode() != null ? payout.getFailureCode() : "payout_failed";
        return message.length() > 255 ? message.substring(0, 255) : message;
    }
}
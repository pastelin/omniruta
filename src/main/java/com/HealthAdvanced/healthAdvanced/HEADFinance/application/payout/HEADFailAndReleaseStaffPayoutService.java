package com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayout;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayoutItem;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutItemRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADFailAndReleaseStaffPayoutService {

    private final HEADStaffPayoutRepository payoutRepository;
    private final HEADStaffPayoutItemRepository payoutItemRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;

    @Transactional
    public HEADStaffPayoutResponse execute(Long payoutId, String note) {
        HEADStaffPayout payout = payoutRepository.findByIdForUpdate(payoutId)
                .orElseThrow(() -> new HEADBadRequestException("Payout no encontrado"));

        if (payout.getStatus() != HEADStaffPayoutStatus.FAILED
                && payout.getStatus() != HEADStaffPayoutStatus.APPROVED
                && payout.getStatus() != HEADStaffPayoutStatus.PROCESSING) {
            throw new HEADBadRequestException(
                    "El payout no se puede liberar en estado " + payout.getStatus()
            );
        }

        List<HEADStaffPayoutItem> items = payoutItemRepository.findByPayout_Id(payout.getId());

        items.forEach(item -> {
            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(item.getJob().getId())
                    .orElseThrow(() -> new HEADBadRequestException(
                            "Snapshot financiero no encontrado para job=" + item.getJob().getId()
                    ));

            if (financial.getPayoutStatus() == HEADJobPayoutStatus.RESERVED) {
                financial.setPayoutStatus(HEADJobPayoutStatus.AVAILABLE);
            }
        });

        payout.setStatus(HEADStaffPayoutStatus.FAILED);
        payout.setNote(buildNote(note));
        payoutRepository.save(payout);

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

    private String buildNote(String note) {
        String base = (note == null || note.trim().isEmpty())
                ? "Payout fallido y liberado"
                : note.trim();

        return base.length() > 255 ? base.substring(0, 255) : base;
    }
}
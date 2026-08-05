package com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HEADApproveStaffPayoutService {

    private final HEADStaffPayoutRepository payoutRepository;

    @Transactional
    public HEADStaffPayoutResponse execute(Long payoutId) {
        var payout = payoutRepository.findByIdForUpdate(payoutId)
                .orElseThrow(() -> new IllegalArgumentException("Payout no encontrado"));

        if (payout.getStatus() != HEADStaffPayoutStatus.REQUESTED) {
            throw new HEADBadRequestException("El payout no se puede aprobar en estado " + payout.getStatus());
        }

        payout.setStatus(HEADStaffPayoutStatus.APPROVED);
        payout.setApprovedAt(Instant.now());

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
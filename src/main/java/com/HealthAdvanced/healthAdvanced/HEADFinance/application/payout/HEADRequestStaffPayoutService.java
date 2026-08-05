package com.HealthAdvanced.healthAdvanced.HEADFinance.application.payout;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.request.HEADRequestPayoutRequest;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADAvailablePayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.api.response.HEADStaffPayoutResponse;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.rule.HEADFinanceRangeResolver;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayout;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADStaffPayoutItem;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADStaffPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFinanceRange;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutItemRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADStaffPayoutRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HEADRequestStaffPayoutService {

    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository staffRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final HEADStaffPayoutRepository payoutRepository;
    private final HEADStaffPayoutItemRepository payoutItemRepository;
    private final HEADFinanceRangeResolver rangeResolver;

    @Transactional(readOnly = true)
    public HEADAvailablePayoutResponse getAvailability(HEADRequestPayoutRequest request) {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = staffRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADFinanceRange range = rangeResolver.resolve(
                request.periodType(),
                request.customFrom(),
                request.customTo()
        );

        BigDecimal total = jobFinancialRepository.sumAvailableForPayout(
                staff.getIdUser(),
                request.currency(),
                HEADJobPayoutStatus.AVAILABLE,
                Instant.now(),
                range.from(),
                range.to()
        );

        return new HEADAvailablePayoutResponse(
                total,
                request.currency(),
                request.periodType().name()
        );
    }

    @Transactional
    public HEADStaffPayoutResponse requestPayout(HEADRequestPayoutRequest request) {
        String staffUuid = jwt.getUserNamePersonalUser();

        var staff = staffRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new HEADBadRequestException("Staff no encontrado"));

        HEADFinanceRange range = rangeResolver.resolve(
                request.periodType(),
                request.customFrom(),
                request.customTo()
        );

        List<HEADJobFinancial> eligible = jobFinancialRepository.findEligibleForPayoutForUpdate(
                staff.getIdUser(),
                request.currency(),
                HEADJobPayoutStatus.AVAILABLE,
                Instant.now(),
                range.from(),
                range.to()
        );

        if (eligible.isEmpty()) {
            throw new HEADBadRequestException("No hay saldo disponible para retiro");
        }

        BigDecimal total = eligible.stream()
                .map(HEADJobFinancial::getStaffPayoutAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        HEADStaffPayout payout = new HEADStaffPayout();
        payout.setStaffUser(staff);
        payout.setCurrency(request.currency());
        payout.setPeriodType(request.periodType());
        payout.setPeriodFrom(range.from());
        payout.setPeriodTo(range.to());
        payout.setItemCount(eligible.size());
        payout.setGrossEligibleAmount(total);
        payout.setAdjustmentsAmount(BigDecimal.ZERO);
        payout.setFinalPayoutAmount(total);
        payout.setStatus(HEADStaffPayoutStatus.REQUESTED);
        payout.setRequestedAt(Instant.now());

        payout = payoutRepository.save(payout);

        HEADStaffPayout savedPayout = payout;

        eligible.stream()
                .map(financial -> {
                    HEADStaffPayoutItem item = new HEADStaffPayoutItem();
                    item.setPayout(savedPayout);
                    item.setJob(financial.getJob());
                    item.setBaseAmount(financial.getStaffPayoutAmount());
                    item.setAdjustmentsAmount(BigDecimal.ZERO);
                    item.setFinalAmount(financial.getStaffPayoutAmount());
                    financial.setPayoutStatus(HEADJobPayoutStatus.RESERVED);
                    return item;
                })
                .forEach(payoutItemRepository::save);

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
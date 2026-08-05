package com.HealthAdvanced.healthAdvanced.HEADFinance.application.adjustment;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADFinance.application.rule.HEADPlatformFeeRuleSelector;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPaymentProcessorRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADPlatformFeeRule;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFeeBearer;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADJobPayoutStatus;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADPaymentProcessor;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.model.HEADFeeCalculationResult;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.policy.HEADFeeCalculationPolicy;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.policy.HEADPayoutEligibilityPolicy;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADPaymentProcessorRuleRepository;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADPlatformFeeRuleRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HEADCalculateJobFinancialService {

    private final HEADJobRepository jobRepository;
    private final HEADJobFinancialRepository jobFinancialRepository;
    private final HEADPlatformFeeRuleSelector platformFeeRuleSelector;
    private final HEADPaymentProcessorRuleRepository paymentProcessorRuleRepository;
    private final HEADFeeCalculationPolicy feeCalculationPolicy;
    private final HEADPayoutEligibilityPolicy payoutEligibilityPolicy;

    @Transactional
    public HEADJobFinancial createSnapshotForCompletedJob(Long jobId, HEADPaymentProcessor processor) {

        if (jobFinancialRepository.existsByJobId(jobId)) {
            return jobFinancialRepository.findById(jobId)
                    .orElseThrow(() -> new HEADBadRequestException("Snapshot inconsistente para job=" + jobId));
        }

        HEADJob job = jobRepository.findByIdForUpdate(jobId)
                .orElseThrow(() -> new HEADBadRequestException("Job no encontrado " + jobId));

        if (job.getStaffUser() == null) {
            throw new HEADBadRequestException("El job no tiene staff asignado");
        }

        if (job.getState() != HEADJobState.COMPLETED || job.getCompletedAt() == null) {
            throw new HEADBadRequestException("El job todavía no está completado");
        }

        if (job.getAmount() == null || job.getAmount().signum() <= 0) {
            throw new HEADBadRequestException("El job no tiene un monto válido");
        }

        Instant effectiveAt = job.getCompletedAt();

        Integer serviceDurationMin = resolveDurationMin(job);

        HEADPlatformFeeRule platformRule = platformFeeRuleSelector.resolve(effectiveAt, serviceDurationMin);

        HEADPaymentProcessorRule processorRule = paymentProcessorRuleRepository
                .findTopByProcessorAndActiveTrueAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(processor, effectiveAt)
                .orElseThrow(() -> new HEADBadRequestException("No existe regla activa para processor " + processor));

        HEADFeeCalculationResult result = feeCalculationPolicy.calculate(
                job.getAmount(),
                platformRule,
                processorRule,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        HEADJobFinancial financial = new HEADJobFinancial();
        financial.setJob(job);
        financial.setStaffUser(job.getStaffUser());
        financial.setProcessor(processor);
        financial.setCurrency(job.getCurrency() != null ? job.getCurrency() : "MXN");

        financial.setGrossAmount(result.grossAmount());

        financial.setPlatformFeePercent(result.platformFeePercent());
        financial.setPlatformFeeFixed(result.platformFeeFixed());
        financial.setPlatformFeeAmount(result.platformFeeAmount());

        financial.setProcessorFeePercent(result.processorFeePercent());
        financial.setProcessorFeeFixed(result.processorFeeFixed());
        financial.setProcessorFeeAmount(result.processorFeeAmount());
        financial.setProcessorFeeBearer(processorRule.getFeeBearer());

        financial.setDirectOperationalCostAmount(BigDecimal.ZERO);
        financial.setDirectCostBearer(HEADFeeBearer.PLATFORM);

        financial.setWithholdingAmount(result.withholdingAmount());
        financial.setStaffPayoutBeforeWithholding(result.staffPayoutBeforeWithholding());
        financial.setStaffPayoutAmount(result.staffPayoutAmount());
        financial.setAppNetAmount(result.appNetAmount());

        financial.setPayoutAvailableAt(
                payoutEligibilityPolicy.resolveAvailableAt(job.getCompletedAt(), platformRule)
        );
        financial.setPayoutStatus(
                payoutEligibilityPolicy.resolveInitialStatus(platformRule)
        );

        financial.setCompletedAt(job.getCompletedAt());
        financial.setCalculatedAt(Instant.now());

        financial.setPlatformRuleId(platformRule.getId());
        financial.setProcessorRuleId(processorRule.getId());

        financial.setProcessorPaymentIntentId(job.getPaymentIntentId());
        financial.setProcessorChargeId(job.getPaymentId());
        financial.setProcessorFeeSynced(false);
        financial.setProcessorFeeDetailsJson("[]");

        job.setStaffPayoutAmount(result.staffPayoutAmount());

        return jobFinancialRepository.save(financial);
    }

    private Integer resolveDurationMin(HEADJob job) {
        if (job.getServiceDurationMin() != null && job.getServiceDurationMin() > 0) {
            return job.getServiceDurationMin();
        }
        throw new HEADBadRequestException("El job no tiene serviceDurationMin");
    }
}
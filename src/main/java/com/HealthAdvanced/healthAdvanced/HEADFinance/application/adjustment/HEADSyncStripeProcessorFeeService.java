package com.HealthAdvanced.healthAdvanced.HEADFinance.application.adjustment;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.entity.HEADJobFinancial;
import com.HealthAdvanced.healthAdvanced.HEADFinance.domain.enums.HEADFeeBearer;
import com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.repository.HEADJobFinancialRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentRetrieveParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADSyncStripeProcessorFeeService {

    private final HEADJobFinancialRepository jobFinancialRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void trySyncRealStripeFee(Long jobId, String paymentIntentId, String chargeId) {
        if (paymentIntentId == null || paymentIntentId.isBlank()) return;
        if (chargeId == null || chargeId.isBlank()) return;

        try {
            Charge charge = Charge.retrieve(chargeId);

            String balanceTransactionId = charge.getBalanceTransaction();
            if (balanceTransactionId == null || balanceTransactionId.isBlank()) {
                return;
            }

            BalanceTransaction balanceTx = BalanceTransaction.retrieve(balanceTransactionId);

            HEADJobFinancial financial = jobFinancialRepository.findByJobIdForUpdate(jobId)
                    .orElseThrow(() -> new HEADBadRequestException("Snapshot financiero no encontrado para job=" + jobId));

            BigDecimal realProcessorFee = amountMinorToDecimal(balanceTx.getFee());
            BigDecimal realProcessorNet = amountMinorToDecimal(balanceTx.getNet());

            financial.setProcessorPaymentIntentId(paymentIntentId);
            financial.setProcessorChargeId(chargeId);
            financial.setProcessorBalanceTransactionId(balanceTransactionId);
            financial.setProcessorFeeAmount(realProcessorFee);
            financial.setProcessorNetAmount(realProcessorNet);
            financial.setProcessorFeeDetailsJson(writeFeeDetails(balanceTx));
            financial.setProcessorFeeSynced(true);
            financial.setProcessorFeeSyncedAt(Instant.now());

            recalculateDerivedAmounts(financial);

            jobFinancialRepository.save(financial);

        } catch (StripeException e) {
            log.warn("No se pudo sincronizar fee real de Stripe para jobId={}: {}", jobId, e.getMessage());
        }
    }

    private String writeFeeDetails(BalanceTransaction bt) {
        try {
            return objectMapper.writeValueAsString(bt.getFeeDetails());
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private BigDecimal amountMinorToDecimal(Long amountMinor) {
        return BigDecimal.valueOf(amountMinor != null ? amountMinor : 0L)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private void recalculateDerivedAmounts(HEADJobFinancial financial) {
        BigDecimal gross = safe(financial.getGrossAmount());
        BigDecimal platformFee = safe(financial.getPlatformFeeAmount());
        BigDecimal processorFee = safe(financial.getProcessorFeeAmount());
        BigDecimal directCost = safe(financial.getDirectOperationalCostAmount());
        BigDecimal withholding = safe(financial.getWithholdingAmount());

        BigDecimal staffBefore = gross.subtract(platformFee);
        BigDecimal appNet = platformFee;

        if (financial.getProcessorFeeBearer() == HEADFeeBearer.STAFF) {
            staffBefore = staffBefore.subtract(processorFee);
        } else if (financial.getProcessorFeeBearer() == HEADFeeBearer.PLATFORM) {
            appNet = appNet.subtract(processorFee);
        }

        if (financial.getDirectCostBearer() == HEADFeeBearer.STAFF) {
            staffBefore = staffBefore.subtract(directCost);
        } else if (financial.getDirectCostBearer() == HEADFeeBearer.PLATFORM) {
            appNet = appNet.subtract(directCost);
        }

        financial.setStaffPayoutBeforeWithholding(scale(staffBefore));
        financial.setStaffPayoutAmount(scale(staffBefore.subtract(withholding)));
        financial.setAppNetAmount(scale(appNet));
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal scale(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }
}
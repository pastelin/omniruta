package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions;

import java.math.BigDecimal;
import java.time.Instant;

public interface HEADStaffEarningTransactionView {
    Long getJobId();
    String getServiceName();
    String getNombre();
    String getPaterno();
    BigDecimal getAmount();
    String getPayoutStatus();
    Instant getCompletedAt();
}
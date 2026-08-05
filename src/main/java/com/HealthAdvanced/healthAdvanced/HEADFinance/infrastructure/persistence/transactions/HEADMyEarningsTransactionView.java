package com.HealthAdvanced.healthAdvanced.HEADFinance.infrastructure.persistence.transactions;

import java.math.BigDecimal;
import java.time.Instant;

public interface HEADMyEarningsTransactionView {
    Long getId();
    String getType();
    String getNombre();
    String getPaterno();
    BigDecimal getAmount();
    String getPayoutStatus();
    Instant getCompletedAt();
}
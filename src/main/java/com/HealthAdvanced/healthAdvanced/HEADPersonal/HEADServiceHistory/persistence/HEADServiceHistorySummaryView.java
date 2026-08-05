package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.persistence;

import java.math.BigDecimal;

public interface HEADServiceHistorySummaryView {
    Long getTotalServices();
    BigDecimal getTotalEarned();
}

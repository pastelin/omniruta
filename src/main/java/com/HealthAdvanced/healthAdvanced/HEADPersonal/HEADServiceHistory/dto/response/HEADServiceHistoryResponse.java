package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADServiceHistory.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record HEADServiceHistoryResponse(
        Integer totalServices,
        BigDecimal totalEarned,
        List<HEADCompletedServiceResponse> services
) {}
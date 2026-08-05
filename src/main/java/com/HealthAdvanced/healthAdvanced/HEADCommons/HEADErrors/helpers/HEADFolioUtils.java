package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.helpers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADErrors.filters.HEADCorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;

public final class HEADFolioUtils {

    private HEADFolioUtils() {}

    public static String getFolio(HttpServletRequest request) {
        Object folio = request.getAttribute(HEADCorrelationIdFilter.FOLIO_KEY);
        return folio != null ? folio.toString() : "NO_FOLIO";
    }
}

package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;

import java.util.List;

public interface HEADJobQueryService {
    /**
     * Devuelve los IDs de jobs en estados activos para el staff dado.
     * "Activos" = ACCEPTED, EN_ROUTE, ARRIVED, STARTED, PAUSED (ajústalo a tu gusto).
     */
    List<Long> findActiveJobIdsForStaffUserId(Long staffUserId);
    List<Long> findActiveJobIdsForClientUserId(Long clientUserId);
    List<HEADJob> findActiveJobCurrentsForStaffUserId(Long staffUserId);
    List<HEADJob> findActiveJobCurrentsForClientUserId(Long clientUserId);
}

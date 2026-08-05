package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackagesPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPackagesToProfilesRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * DTO que encapsula la información de una nueva oferta de servicio enviada a un conductor.
 */

public record HEADOfferDto(
        Long jobId,
        String clientName,
        String tripStartLocation,
        String tripEndLocation,
        String estimatedPayout,
        String currency,
        Double distanceToPickupMeters,
        Integer estimatedDurationMinutes,
        Instant offerExpiresAt,
        long timeRemainSeconds,
        String titlePackage,
        String subtitlePackage,
        HEADServiceMode serviceMode
) {
    /**
     * Mapea una entidad HEADJob a OfferDto.
     * ASUME que job.request y job.client están cargados.
     */
    public static HEADOfferDto of(
            HEADJob job,
            HEADPackagesPersonal packageType,
            String startAddress,
            String endAddress
    ) {
        Instant expiresAt = job.getOfferExpiresAt();

        Long timeRemainingSeconds = null;
        Long expiresEpoch = null;

        var payout = job.getAmount() == null ? null
                : job.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();

        if (expiresAt != null) {
            expiresEpoch = expiresAt.getEpochSecond();
            long remaining = expiresEpoch - Instant.now().getEpochSecond();
            timeRemainingSeconds = Math.max(0, remaining);
        }

        String clientName = null;
        if (job.getClient() != null) {
            clientName = job.getClient().getNombre() + " " + job.getClient().getAPaterno();
        }

        return new HEADOfferDto(
                job.getId(),
                clientName,
                startAddress,
                endAddress,
                payout,
                job.getCurrency(),
                job.getDistanceKmBucket(),
                job.getDurationMinBucket(),

                expiresAt,
                timeRemainingSeconds != null ? timeRemainingSeconds : 0L,

                packageType != null ? packageType.getTitle() : null,
                packageType != null ? packageType.getSubtitle() : null,
                job.getServiceMode()
        );
    }

}
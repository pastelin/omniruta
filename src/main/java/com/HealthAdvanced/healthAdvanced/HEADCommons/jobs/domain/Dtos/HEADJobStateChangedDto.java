package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADCancelReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStaffJobMaterialDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPackageOption;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.enums.HEADReviewState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff.HEADNameFormatters;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record HEADJobStateChangedDto(
        Long jobId,
        String prevState,
        String nextState,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant changedAt,

        String changedBy,        // "SYSTEM" | "STAFF" | "CLIENT"
        String reason,           // opcional (cancelReason, etc.)

        Long requestId,          // opcional, útil para correlación
        Long clientId,           // opcional
        String staffUuid,
        String clientUuid,// opcional

        String amount,       // opcional
        String currency,         // opcional

        Double clientLat,        // opcional (mapa)
        Double clientLng,        // opcional

        Long distanceMeters,  // opcional (si ya lo calculaste)
        Long durationSeconds, // opcional
        String endAddress,
        String nameClient,
        String nameStaff,
        String avatarUrlClient,
        String avatarUrlStaff,
        String serviceName,
        Long version, // para idempotencia en front si usas @Version
        HEADServiceMode serviceMode,
        HEADReviewState reviewState,
        String licenseNo,
        HEADOccupationCode code,
        Integer yearsClient,
        String gender,
        HEADCancelReason cancelReason,
        String urlPrescription,
        List<HEADStaffJobMaterialDto> materials,
        String idPackage,
        Long packageOptionId,
        Long profileId

) {
    // Factory conveniente si quieres construirlo desde la entidad
    public static HEADJobStateChangedDto of(
            HEADJob job,
            String prevState,
            Instant at,
            String actorUuid,
            String avatarUrlClient,
            String avatarStaff,
            String licenseNo,
            HEADOccupationCode code,
            Integer yearClient,
            String urlPrescription,
            List<HEADStaffJobMaterialDto> materials
    ) {
        var payout = job.getAmount() == null ? null
                : job.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        var nameClient = job.getClient().getNombre() + " " + job.getClient().getAPaterno();
        var staff = job.getStaffUser();
        var nameStaff = HEADNameFormatters.buildStaffDisplayName(staff, code);
        return new HEADJobStateChangedDto(
                job.getId(),
                prevState,
                job.getState() != null ? job.getState().name() : null,
                at,
                actorUuid,
                job.getCancelReason() != null ? job.getCancelReason().name() : null,
                job.getRequest().getIdServiceRequestClient(),
                job.getClient().getIdUser(),
                job.getStaffUuid() == null ? null : job.getStaffUuid(),
                job.getClient().getUuIdUser(),
                payout,
                job.getCurrency(),
                job.getClientLat(),
                job.getClientLng(),
                job.getDistanceMeters(),
                job.getDurationSeconds(),
                job.getEndAddress(),
                nameClient,
                nameStaff,
                avatarUrlClient,
                avatarStaff,
                job.getRequest().getPkg().getTitle(),
                job.getVersion(),
                job.getServiceMode(),
                job.getReviewState(),
                licenseNo,
                code,
                yearClient,
                job.getClient().getIdSexUser() != null ? job.getClient().getIdSexUser().getTypeSex() : null,
                job.getCancelReason(),
                urlPrescription,
                materials,
                job.getRequest().getPkg().getId(),
                job.getRequest().getPackageOption().getId(),
                job.getRequest().getIdProfile()
        );
    }

    public static HEADJobStateChangedDto modifiedForClientOf(HEADJobStateChangedDto dto) {
        return new HEADJobStateChangedDto(
                dto.jobId(),
                dto.prevState(),
                dto.nextState(),
                dto.changedAt(),
                dto.changedBy(),
                dto.reason(),
                dto.requestId(),
                dto.clientId(),
                dto.staffUuid(),
                dto.clientUuid(),
                dto.amount(),
                dto.currency(),
                dto.clientLat(),
                dto.clientLng(),
                dto.distanceMeters(),
                dto.durationSeconds(),
                dto.endAddress(),
                dto.nameClient(),
                dto.nameStaff(),
                dto.avatarUrlClient(),
                dto.avatarUrlStaff(),
                dto.serviceName(),
                dto.version(),
                dto.serviceMode(),
                dto.reviewState(),
                dto.licenseNo(),
                dto.code(),
                dto.yearsClient(),
                dto.gender(),
                dto.cancelReason(),
                null,
                new ArrayList<>(),
                dto.idPackage(),
                dto.packageOptionId(),
                dto.profileId()
        );
    }
}

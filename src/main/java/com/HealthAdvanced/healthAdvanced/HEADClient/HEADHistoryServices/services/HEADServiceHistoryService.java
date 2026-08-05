package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.services;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.utils.HEADPromotionUtils;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.dto.response.HEADServiceHistoryGenericResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADJobRepository;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.repository.HEADStaffReviewRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADCategory;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADEnums.HEADOwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class HEADServiceHistoryService {

    private final HEADJobRepository jobRepo;
    private final HEADClientsRepository clientsRepo;
    private final HEADStaffReviewRepository reviewRepo;
    private final HEADJwtGenerator jwt;

    private static final Set<HEADJobState> CANCELLED_STATES = Set.of(
            HEADJobState.CANCELLED,
            HEADJobState.REJECTED,
            HEADJobState.EXPIRED,
            HEADJobState.WITHDRAWN,
            HEADJobState.UNASSIGNABLE
    );
    private static final Set<HEADJobState> COMPLETED_STATES = Set.of(HEADJobState.COMPLETED);

    private static Set<HEADJobState> statesForFilter(String statusKey) {
        if (statusKey == null || statusKey.isBlank() || statusKey.equalsIgnoreCase("ALL")) return Set.of(); // ALL literal
        return switch (statusKey.toUpperCase()) {
            case "COMPLETED" -> COMPLETED_STATES;
            case "CANCELLED" -> CANCELLED_STATES;
            default -> Set.of();
        };
    }

    @Transactional(readOnly = true)
    public HEADServiceHistoryGenericResponse getHistory(String q, String status, Long occupationProfileId, int page, int size) {

        String clientUuid = jwt.getUserNamePersonalUser();

        long clientId = clientsRepo.findByUuIdUser(clientUuid)
                .orElseThrow(() -> new IllegalArgumentException("Client not found for uuid: " + clientUuid))
                .getIdUser();

        double avg = reviewRepo.avgRatingForClientId(clientId);
        double avgRounded = Math.round(avg * 10.0) / 10.0;

        Set<HEADJobState> states = statesForFilter(status);
        boolean statesEmpty = states.isEmpty();

        var pageable = PageRequest.of(page, size);
        var p = jobRepo.findServiceHistoryGenericFiltered(
                clientUuid,
                q,
                states,
                statesEmpty,
                occupationProfileId,
                HEADOwnerType.SYSTEM,
                HEADCategory.SERVICE_ICON,
                pageable
        );


        int total = (int) p.getTotalElements();

        int completed = (int) jobRepo.countByClientUuidAndStates(clientUuid, COMPLETED_STATES);

        var items = p.getContent().stream().map(r -> {
            var pro = toProfessional(r.getProfessionalName(), r.getProfessionalSpecialty(), r.getJobState());

            Float rating = (r.getRating() == null) ? null : r.getRating().floatValue();

            var promoTags = HEADPromotionUtils.parseTagsJson(r.getIconTags());
            var gradientHex = (promoTags != null) ? promoTags.gradientHex : null;

            return new HEADServiceHistoryGenericResponse.Item(
                    r.getId(),
                    safe(r.getPackageId()),
                    safe(r.getServiceName()),
                    safe(r.getCategoryLabel()),
                    safe(r.getJobState()),
                    r.getWhen(),
                    safe(r.getLocation()),
                    r.getAmount(),
                    safe(r.getCurrency()),
                    pro,
                    rating,
                    safe(r.getNotes()),
                    r.getIconUrl(),
                    gradientHex,
                    r.getOccupationProfileId()
            );
        }).toList();

        var pageResp = new HEADPageResponse<>(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.hasNext(),
                p.hasNext() ? p.getNumber() + 1 : null
        );

        return new HEADServiceHistoryGenericResponse(
                new HEADServiceHistoryGenericResponse.Summary(total, completed, avgRounded),
                pageResp
        );
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        var parts = fullName.trim().split("\\s+");
        String a = parts.length > 0 && !parts[0].isBlank() ? parts[0].substring(0, 1) : "";
        String b = parts.length > 1 && !parts[1].isBlank() ? parts[1].substring(0, 1) : "";
        return (a + b).toUpperCase(Locale.ROOT);
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    private HEADServiceHistoryGenericResponse.Professional toProfessional(
            String professionalName,
            String professionalSpecialty,
            String jobState
    ) {
        boolean missing = professionalName == null || professionalName.isBlank();

        // si no hubo staff y terminó cancelado/expirado/unassignable etc.
        if (missing) {
            return new HEADServiceHistoryGenericResponse.Professional(
                    "Sin personal asignado",
                    "—",
                    "?"
            );
        }

        String name = professionalName.trim();
        String specialty = (professionalSpecialty == null || professionalSpecialty.isBlank()) ? "—" : professionalSpecialty.trim();

        return new HEADServiceHistoryGenericResponse.Professional(
                name,
                specialty,
                initials(name)
        );
    }
}
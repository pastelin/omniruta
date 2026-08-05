package com.HealthAdvanced.healthAdvanced.HEADAdmin.service;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminPendingReviewItemResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.shared.HEADRoles;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEHOUtilsFile.HEHOFileUploadUtil;
import com.HealthAdvanced.healthAdvanced.ModelsBD.DocumentsRepository.HEADDocumentsRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HEADAdminPendingReviewService {

    private final HEADPersonalUserRepository headPersonalUserRepository;
    private final HEADDocumentsRepository headDocumentsRepository;
    private final HEHOFileUploadUtil hehoFileUploadUtil;

    @Transactional(readOnly = true)
    public List<HEADAdminPendingReviewItemResponse> list(Boolean canGoOnlineFilter) {
        List<HEADPersonalUser> staffList =
                headPersonalUserRepository.findStaffPendingReview(HEADRoles.REGISTER_PERSONAL);

        return staffList.stream()
                .map(this::toItem)
                .filter(item -> canGoOnlineFilter == null || Objects.equals(item.getCanGoOnline(), canGoOnlineFilter))
                .sorted(Comparator
                        .comparing(HEADAdminPendingReviewItemResponse::getCanGoOnline).reversed()
                        .thenComparing(HEADAdminPendingReviewItemResponse::getLastUploadedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private HEADAdminPendingReviewItemResponse toItem(HEADPersonalUser staff) {
        Long userId = staff.getIdUser();

        List<Long> profileIds = staff.getOccupationLinks() == null
                ? List.of()
                : staff.getOccupationLinks().stream()
                .map(HEHOOccupationPersonalUser::getIdOccupationProfile)
                .filter(Objects::nonNull)
                .map(HEADOccupationProfile::getIdOccupationProfile)
                .filter(Objects::nonNull)
                .toList();

        boolean canGoOnline = !profileIds.isEmpty() &&
                profileIds.stream().allMatch(profileId -> hehoFileUploadUtil.canGoOnline(userId, profileId));

        Long totalUploadedDocs = headDocumentsRepository.countByIdUser_IdUserAndActiveTrue(userId);
        Long pendingDocs = headDocumentsRepository.countByIdUser_IdUserAndActiveTrueAndStatus(userId, HEADDocumentStatus.PENDING);
        Long approvedDocs = headDocumentsRepository.countByIdUser_IdUserAndActiveTrueAndStatus(userId, HEADDocumentStatus.APPROVED);
        Long rejectedDocs = headDocumentsRepository.countByIdUser_IdUserAndActiveTrueAndStatus(userId, HEADDocumentStatus.REJECTED);

        return HEADAdminPendingReviewItemResponse.builder()
                .userId(userId)
                .uidUser(staff.getUidUser())
                .fullName(buildFullName(staff))
                .email(staff.getEmail())
                .phone(staff.getTelefono())
                .currentRole(staff.getRoles())
                .isEnabled(Boolean.TRUE.equals(staff.getIsEnabled()))
                .canGoOnline(canGoOnline)
                .canEnableAccess(canGoOnline && !Boolean.TRUE.equals(staff.getIsEnabled()))
                .totalUploadedDocs(totalUploadedDocs)
                .pendingDocs(pendingDocs)
                .approvedDocs(approvedDocs)
                .rejectedDocs(rejectedDocs)
                .lastUploadedAt(headDocumentsRepository.findLastUploadedAtByUser(userId))
                .build();
    }

    private String buildFullName(HEADPersonalUser staff) {
        return Stream.of(staff.getNombre(), staff.getAPaterno(), staff.getAMaterno())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}
package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HEADAdminPendingReviewItemResponse {
    private Long userId;
    private String uidUser;
    private String fullName;
    private String email;
    private String phone;

    private String currentRole;
    private Boolean isEnabled;

    private Boolean canGoOnline;
    private Boolean canEnableAccess;

    private Long totalUploadedDocs;
    private Long pendingDocs;
    private Long approvedDocs;
    private Long rejectedDocs;

    private LocalDateTime lastUploadedAt;
}
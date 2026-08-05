package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminStaffDocumentDetailResponse;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADDocumentStatus;

import java.util.List;

public interface HEADAdminStaffDocumentsService {

    List<HEADAdminStaffDocumentDetailResponse> getDocumentsByStaff(
            Long userId,
            HEADDocumentStatus status,
            Long occProfileId
    );
}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

import java.util.List;

public record HEADStaffJobMaterialsResponse(
        Long jobId,
        String packageId,
        String packageTitle,
        Long packageOptionId,
        String optionLabel,
        Boolean includesMaterials,
        List<HEADStaffJobMaterialDto> materials
) { }

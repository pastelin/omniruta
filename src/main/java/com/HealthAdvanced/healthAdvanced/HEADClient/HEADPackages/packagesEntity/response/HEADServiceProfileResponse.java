package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response;

import java.util.List;

public record HEADServiceProfileResponse(
        Long idProfileId,
        String nameProfile,
        List<String> color,
        String iconUrl
) {
}

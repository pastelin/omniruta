package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response;

import java.util.List;

public record HEADPackageAvailableResponse(
        HEADServiceProfileResponse profileService,
        List<HEADPackageAvailableDto> packageAvailable
) {
}

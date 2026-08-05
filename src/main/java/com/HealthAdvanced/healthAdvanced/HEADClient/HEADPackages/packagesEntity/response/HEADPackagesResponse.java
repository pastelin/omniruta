package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HEADPackagesResponse {
    private List<HEADPackageAvailableDto> packageAvailableDtoList;
}

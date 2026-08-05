package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import java.util.List;

public record HEADAddressComponent(
        String long_name,
        String short_name,
        List<String> types
) {
}

package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADGeocoding.entity.response;

import java.util.List;

public record HEADGoogleResult(
    String formatted_address,
    String place_id,
    List<String> types,
    List<HEADAddressComponent> address_components
) { }

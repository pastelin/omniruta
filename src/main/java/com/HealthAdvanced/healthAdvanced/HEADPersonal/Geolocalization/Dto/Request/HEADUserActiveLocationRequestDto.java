package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADUserActiveLocationRequestDto {
    private Double latitude;
    private Double longitude;
}

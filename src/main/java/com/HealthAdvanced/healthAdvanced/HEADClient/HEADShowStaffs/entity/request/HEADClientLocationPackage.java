package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HEADClientLocationPackage {
    private Long jobId;
    private double userLat;
    private double userLong;
    private String idPackage;
}

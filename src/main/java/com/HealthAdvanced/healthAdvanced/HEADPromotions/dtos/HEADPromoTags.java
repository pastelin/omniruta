package com.HealthAdvanced.healthAdvanced.HEADPromotions.dtos;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.enums.HEADUiActionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HEADPromoTags {
    public Integer v;             // versión opcional
    public HEADUiActionType action;         // OPEN_SERVICE | OPEN_PACKAGE | ...
    public String serviceId;
    public String packageId;
    public String coupon;
    public String url;
    public String iconText;
    public java.util.List<String> gradientHex;
}

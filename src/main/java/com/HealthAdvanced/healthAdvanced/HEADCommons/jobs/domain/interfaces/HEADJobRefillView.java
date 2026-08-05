package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADServiceMode;

public interface HEADJobRefillView {
    Long getId();
    Double getClientLat();
    Double getClientLng();
    String getPkgId();
    HEADServiceMode getServiceMode();
}



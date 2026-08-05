package com.HealthAdvanced.healthAdvanced.HEADClient.HEADHistoryServices.interfaces;

import java.math.BigDecimal;
import java.time.Instant;


public interface HEADServiceHistoryRowViewGeneric {
    Long getId();
    String getPackageId();
    String getServiceName();
    String getCategoryLabel();
    String getJobState();
    Instant getWhen();
    String getLocation();
    BigDecimal getAmount();
    String getCurrency();
    String getProfessionalName();
    String getProfessionalSpecialty();
    Integer getRating();
    String getNotes();
    Long getOccupationProfileId();  // alias: occupationProfileId
    String getIconUrl();            // alias: iconUrl
    String getIconTags();           // alias: iconTags
}
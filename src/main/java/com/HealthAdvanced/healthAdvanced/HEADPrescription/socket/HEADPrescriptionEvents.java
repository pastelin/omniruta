package com.HealthAdvanced.healthAdvanced.HEADPrescription.socket;

public final class HEADPrescriptionEvents {
    private HEADPrescriptionEvents() {}

    public static final String PRESCRIPTION_DRAFT_JOIN    = "PRESCRIPTION_DRAFT_JOIN";
    public static final String PRESCRIPTION_DRAFT_UPSERT  = "PRESCRIPTION_DRAFT_UPSERT";
    public static final String PRESCRIPTION_DRAFT_UPDATED = "PRESCRIPTION_DRAFT_UPDATED";

    public static final String PRESCRIPTION_ISSUE         = "PRESCRIPTION_ISSUE";
    public static final String PRESCRIPTION_ISSUED        = "PRESCRIPTION_ISSUED";

    public static final String PRESCRIPTION_ERROR         = "PRESCRIPTION_ERROR";
    public static final String DESCRIPTION_OK = "DESCRIPTION_OK";
}



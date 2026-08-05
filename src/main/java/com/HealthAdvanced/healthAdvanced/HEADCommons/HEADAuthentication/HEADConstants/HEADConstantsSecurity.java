package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADConstants;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.claims.HEADClaims;

public final class HEADConstantsSecurity {
    private HEADConstantsSecurity() {}
    public static final String AUTHORIZATION_HEADER = HEADClaims.AUTHORIZATION;
    public static final String BEARER_TOKEN = HEADClaims.BEARER;
    public static final String ROLES = HEADClaims.ROLES;
    public static final String REGISTER_CLIENT = "REGISTER_CLIENT";
    public static final String REGISTER_PERSONAL = "REGISTER_PERSONAL";
    public static final String ACCESS_CLIENT = "ACCESS_CLIENT";
}
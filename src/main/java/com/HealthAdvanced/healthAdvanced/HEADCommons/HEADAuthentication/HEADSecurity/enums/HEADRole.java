package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.enums;

public enum HEADRole {
    REGISTER_PERSONAL,
    ACCESS_PERSONAL,
    REGISTER_CLIENT,
    ACCESS_CLIENT,
    ACCESS_ADMIN_HEAD;

    public String asAuthority() {             // "ROLE_ACCESS_CLIENT" para Spring
        return "ROLE_" + name();
    }
}

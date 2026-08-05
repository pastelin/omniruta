package com.HealthAdvanced.healthAdvanced.ModelsBD.utils;

import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;

public final class HEADSexUtil {
    private HEADSexUtil() {}

    public static boolean isFemale(HEADSexUser sex) {
        return sex != null && sex.getIdSexUser() != null && sex.getIdSexUser() == 1L;
    }

    public static boolean isMale(HEADSexUser sex) {
        return sex != null && sex.getIdSexUser() != null && sex.getIdSexUser() == 2L;
    }
}


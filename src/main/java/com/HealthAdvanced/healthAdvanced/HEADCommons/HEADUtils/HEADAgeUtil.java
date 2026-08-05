package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils;

import java.time.LocalDate;
import java.time.Period;

public class HEADAgeUtil {

    public static int ageYears(LocalDate birthDate) {
        return ageYears(birthDate, LocalDate.now());
    }

    private static Integer ageYears(LocalDate birthDate, LocalDate today) {
        if (birthDate == null) return null;
        if (birthDate.isAfter(today)) return null;
        return Period.between(birthDate, today).getYears();
    }
}


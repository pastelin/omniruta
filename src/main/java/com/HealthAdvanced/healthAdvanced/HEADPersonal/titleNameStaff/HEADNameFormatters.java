package com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;

public final class HEADNameFormatters {
    private HEADNameFormatters() {}

    public static String buildStaffDisplayName(HEADPersonalUser staff, HEADOccupationCode occCode) {
        if (staff == null) return null;

        String prefix = HEADTitleResolver.resolve(occCode, staff.getIdSexUser()).text();

        String fullName = joinNames(staff.getNombre(), staff.getAPaterno(), staff.getAMaterno());
        return prefix.isBlank() ? fullName : prefix + " " + fullName;
    }

    public static String buildStaffNameLastName(HEADPersonalUser staff, HEADOccupationCode occCode) {
        if (staff == null) return null;

        String prefix = HEADTitleResolver.resolve(occCode, staff.getIdSexUser()).text();

        String fullName = joinNameAndLastName(staff.getNombre(), staff.getAPaterno(), staff.getAMaterno());
        return prefix.isBlank() ? fullName : prefix + " " + fullName;
    }

    private static String joinNames(String nombre, String paterno, String materno) {
        String n = safe(nombre);
        String p = safe(paterno);
        String m = safe(materno);
        return (n + " " + p + " " + m).trim().replaceAll("\\s+", " ");
    }


    private static String joinNameAndLastName(String nombre, String paterno, String materno) {
        String n = safe(nombre);
        String p = safe(paterno);
        return (n + " " + p).trim().replaceAll("\\s+", " ");
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.titleNameStaff;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.enums.HEADOccupationCode;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Enums.HEADTitlePrefix;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.utils.HEADSexUtil;

public final class HEADTitleResolver {
    private HEADTitleResolver() {}

    public static HEADTitlePrefix resolve(HEADOccupationCode code, HEADSexUser sex) {
        boolean female = HEADSexUtil.isFemale(sex);

        if (code == null) return HEADTitlePrefix.NONE;

        return switch (code) {
            case DOCTOR    -> female ? HEADTitlePrefix.DRA  : HEADTitlePrefix.DR;
            case NURSE     -> female ? HEADTitlePrefix.ENFA : HEADTitlePrefix.ENF;
            case CAREGIVER -> HEADTitlePrefix.CUID;
            case THERAPIST -> HEADTitlePrefix.TER;
        };
    }
}

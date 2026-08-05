package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.entity;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.enums.HEADTypeUser;

public record HEADUserInfo (
        String uuidUser,
        Long idUser,
        HEADTypeUser typeUser
){
}

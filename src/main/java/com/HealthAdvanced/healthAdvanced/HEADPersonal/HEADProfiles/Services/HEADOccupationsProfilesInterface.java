package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Services;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsProfilesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADServiceProfileItemDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface HEADOccupationsProfilesInterface {
    HEADOccupationsProfilesDto headSetOccupations();
    ResponseEntity<?> saveOccupationPersonalUser(List<Long> idOccupationProfile);
    List<HEADServiceProfileItemDto> listProfilesUber();
}

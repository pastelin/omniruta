package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.ServicesMap;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsProfilesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsResponse;

import java.util.List;

public interface HEADOccupationProfileMapInterface {
    HEADOccupationsProfilesDto headOccupationsProfilesMapDto(
            List<HEADOccupations> headOccupations, List<HEADOccupationProfile> headOccupationProfiles);
    HEADOccupationsResponse occupationsResponse(HEADOccupations headOccupations, List<HEADOccupationProfile> headOccupationProfileResponses);
    HEADOccupationProfileResponse headOccupationProfileResponse(HEADOccupationProfile occupationProfile);
    HEHOOccupationPersonalUser headOccupationPersonaluserMaps(HEADPersonalUser headPersonalUser, HEADOccupationProfile headOccupationProfile);
}

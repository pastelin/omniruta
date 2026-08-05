package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.ServicesMap;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupationProfile;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADOccupations;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEHOOccupationPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsProfilesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class HEADOccupationProfileMapService implements HEADOccupationProfileMapInterface {

    @Override
    public HEADOccupationsProfilesDto headOccupationsProfilesMapDto(
            List<HEADOccupations> headOccupations, List<HEADOccupationProfile> headOccupationProfiles) {
        List<HEADOccupationsResponse> occupationsResponses = new ArrayList<>();
        headOccupations.stream().forEach(occupation -> {
            var occupations = occupationsResponse(occupation,headOccupationProfiles);
            occupationsResponses.add(occupations);
        });
        var headProfiles = new HEADOccupationsProfilesDto();
        headProfiles.setHeadOccupations(occupationsResponses);
        return headProfiles;
    }

    @Override
    public HEADOccupationsResponse occupationsResponse(HEADOccupations headOccupations, List<HEADOccupationProfile> headOccupationProfileResponses) {
        var headOccupationsResponse = new HEADOccupationsResponse();
        var headOccupationProfile = new ArrayList<HEADOccupationProfileResponse>();
        headOccupationsResponse.setIdOccupation(headOccupations.getIdOccupation());
        headOccupationsResponse.setNameOccupation(headOccupations.getNameOccupation());

        headOccupationProfileResponses.stream().filter(occupationProfile -> occupationProfile.getIdOccupation().equals(headOccupations)).forEach(occupationProfile -> {
            headOccupationProfile.add(headOccupationProfileResponse(occupationProfile));
        });
        headOccupationsResponse.setHeadOccupationsProfiles(headOccupationProfile);
        return headOccupationsResponse;
    }

    @Override
    public HEADOccupationProfileResponse headOccupationProfileResponse(HEADOccupationProfile occupationProfile) {
        var headOccupationProfile = new HEADOccupationProfileResponse();
        headOccupationProfile.setIdOccupation(occupationProfile.getIdOccupation().getIdOccupation());
        headOccupationProfile.setNameTypeProfile(occupationProfile.getNameTypeProfile());
        headOccupationProfile.setIdOccupationProfile(occupationProfile.getIdOccupationProfile());
        return headOccupationProfile;
    }

    @Override
    public HEHOOccupationPersonalUser headOccupationPersonaluserMaps(HEADPersonalUser headPersonalUser, HEADOccupationProfile headOccupationProfile) {
        HEHOOccupationPersonalUser headOccupationProfiles = new HEHOOccupationPersonalUser();
        headOccupationProfiles.setIdOccupationProfile(headOccupationProfile);
        headOccupationProfiles.setIdPersonalUser(headPersonalUser);
        return headOccupationProfiles;
    }
}

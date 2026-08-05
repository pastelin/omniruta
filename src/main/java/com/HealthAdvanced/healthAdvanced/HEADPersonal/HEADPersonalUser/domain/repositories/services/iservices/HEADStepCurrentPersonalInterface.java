package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.iservices;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADStepCurrentPersonalResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADStepCurrentPersonal;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.Dtos.HEADStatusResponseDTO;
import com.HealthAdvanced.healthAdvanced.HEADStepCurrentFlow.ModelsBD.HEADStepSubCatalogue;

import java.util.List;

public interface HEADStepCurrentPersonalInterface {
    HEADStatusResponseDTO statusStaff(Long staffId);
    void staffCompleteSub(Long staffId, String parentStepName, String subStepName);
    void staffCompleteStep(Long staffId, String stepName);
    HEADStepSubCatalogue getStepSubNext(String stepName, String subStepName);
    static final String STAFF  = "STAFF";
   /* HEADStepCurrentPersonal saveStepCurrent(HEADStepCurrentPersonal headStepCurrentPersonal);
    HEADStepCurrentPersonalResponse findIdPersonalUser(HEADPersonalUser headPersonalUser);
    Boolean ExistsStepCurrentPersonal(Integer idStepCurrent);
    List<HEADStepCurrentPersonal> headStepCurrentPersonalUser(HEADPersonalUser headPersonalUser);*/

}

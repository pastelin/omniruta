package com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminCreateRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminLoginRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminResponse;

import java.util.List;

public interface HEADAdminService {

    HEADAdminResponse createAdmin(HEADAdminCreateRequest request);

    HEADAdminLoginResponse login(HEADAdminLoginRequest request);

    List<HEADAdminResponse> findAll();
}

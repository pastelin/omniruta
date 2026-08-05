package com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.controller;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.service.HEADShowStaffsToClientsService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessagePersonal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/client/staffsToClient")
public class HEADShowStaffsController {
    @Autowired
    private HEADShowStaffsToClientsService headShowStaffsToClientsService;
}

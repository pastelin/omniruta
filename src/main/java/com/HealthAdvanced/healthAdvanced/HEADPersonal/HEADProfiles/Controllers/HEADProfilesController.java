package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Controllers;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Request.HEADOccupationsPersonalUserRequest;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationPersonalUserResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADOccupationsProfilesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Entities.Response.HEADServiceProfileItemDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADProfiles.Services.HEADOccupationsProfilesInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/register/personal/v1/profiles")
public class HEADProfilesController implements HEADProfilesControllerInterface {
    @Autowired
    @Qualifier("HEADOccupationProfilesService")
    private HEADOccupationsProfilesInterface headOccupationsProfilesInterface;

    @Operation(summary = "Obtener el listado de perfiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se obtiene con exito el listado de perfiles",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADOccupationsProfilesDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Error al obtener el listado de perfiles",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADErrorMessageClient.class))
                    })
    })
    @GetMapping("/OccupationsProfiles")
    @Override
    public HEADOccupationsProfilesDto headOccupationsProfilesDto() {
        return headOccupationsProfilesInterface.headSetOccupations();
    }
    @Operation(summary = "Perfil de usbbuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guardar el perfil con exito",
            content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = HEADOccupationPersonalUserResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Al guardar el perfil del usuario fallo",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HEADErrorMessageClient.class))
            })
    })
    @PostMapping("/saveProfileUser")
    public ResponseEntity<?> headSaveOccupationProfile(@RequestBody HEADOccupationsPersonalUserRequest headOccupationsPersonalUserRequest) {
        return headOccupationsProfilesInterface.saveOccupationPersonalUser(headOccupationsPersonalUserRequest.getIdOccupationProfile());
    }

    @GetMapping("/listProfiles")
    public ResponseEntity<List<HEADServiceProfileItemDto>> listProfilesUber() {
        return ResponseEntity.ok(headOccupationsProfilesInterface.listProfilesUber());
    }
}

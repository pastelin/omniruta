package com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesController;

import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackageAvailableDto;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesEntity.response.HEADPackageAvailableResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADPackages.packagesService.HEADPackagesService;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationCurrent;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.request.HEADClientLocationPackage;
import com.HealthAdvanced.healthAdvanced.HEADClient.HEADShowStaffs.entity.response.HEADStaffsActivesDto;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessagePersonal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/packagesToProfile")
public class HEADPackagesController {
    @Autowired
    private HEADPackagesService headPackagesService;

    @Operation(summary = "Mostrar los paquetes disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mostrar los paquetes disponibles",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADStaffsActivesDto.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Error al obtener los paquetes disponibles",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADErrorMessagePersonal.class))
                    })
    })
    @PostMapping("/packagesAvailable")
    public ResponseEntity<?> getPackagesToProfiles(@RequestBody HEADClientLocationPackage headClientLocationCurrent) {
        return headPackagesService.showPackagesAvailable(headClientLocationCurrent);
    }

    @GetMapping("/{profileId}/packages")
    public HEADPackageAvailableResponse getPackages(@PathVariable Long profileId) {
        return headPackagesService.listByProfile(profileId);
    }
}

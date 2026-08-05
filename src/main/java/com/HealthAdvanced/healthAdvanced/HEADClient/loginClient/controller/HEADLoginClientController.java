package com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.controller;


import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response.HEADSuccessResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADLoginPasswordRequest;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.request.HEADResetPassword;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.entity.response.HEADUsersClientsResponse;
import com.HealthAdvanced.healthAdvanced.HEADClient.loginClient.service.HEADLoginClientService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.entity.HEADWSRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessageClient;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADErrorMessagePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response.HEADJwtUsersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/authentication/client")
public class HEADLoginClientController {
    @Autowired
    private HEADLoginClientService headLoginClientService;

    @Operation(summary = "Inicio de sesión del Cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión correctamente",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADJwtUsersResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Error al iniciar sesión el cliente",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADErrorMessagePersonal.class))
                    })
    })
    @PostMapping("/login")
    public ResponseEntity<?> headLoginClient(@RequestBody HEADLoginPasswordRequest headClientRegisterRequestDto) {
            return headLoginClientService.loginClientAuth(headClientRegisterRequestDto);
    }

    /*@Operation(summary = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resetear password correctamente",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADApiResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Error al iniciar sesión el cliente",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = HEADErrorMessagePersonal.class))
                    })
    })
    @PostMapping("/resetPassword")
    public ResponseEntity<HEADApiResponse<HEADSuccessResetPassword>> headResetPassword(
            @Valid @RequestBody HEADWSRequest<HEADResetPassword> req
    ) {
        return headLoginClientService.resetPasswordClient(req.transaction());
    }*/
}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity.HEADBulkRequirementsResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.entity.HEADRequirementsByProfileResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.DocumentsPersonal.service.HEADStaffRequirementsService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/staff/profile")
@RequiredArgsConstructor
public class HEADStaffProfileController {
    private final HEADStaffRequirementsService requirementsService;
    private final HEADJwtGenerator jwt;
    private final HEADPersonalUserRepository users;

    @GetMapping("/requirements")
    public ResponseEntity<HEADRequirementsByProfileResponse> requirements(
            @RequestParam Long occupationProfileId) {

        var uid = jwt.getUserNamePersonalUser();
        var user = users.findByUidUser(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        var resp = requirementsService.getRequirementsDetailed(user.getIdUser(), occupationProfileId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/requirements/bulk")
    public ResponseEntity<HEADBulkRequirementsResponse> requirementsBulk() {

        var uid = jwt.getUserNamePersonalUser();
        var user = users.findByUidUser(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var listRequiredDocs = requirementsService.getRequirementsBulk(user);

        return ResponseEntity.ok(new HEADBulkRequirementsResponse(listRequiredDocs));
    }
}

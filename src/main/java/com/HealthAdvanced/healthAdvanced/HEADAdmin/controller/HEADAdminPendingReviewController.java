package com.HealthAdvanced.healthAdvanced.HEADAdmin.controller;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminPendingReviewItemResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.service.HEADAdminPendingReviewService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class HEADAdminPendingReviewController {

    private final HEADAdminPendingReviewService headAdminPendingReviewService;

    @GetMapping("/pending-review")
    public ResponseEntity<HEADApiResponse<List<HEADAdminPendingReviewItemResponse>>> list(
            @RequestParam(required = false) Boolean canGoOnline
    ) {
        List<HEADAdminPendingReviewItemResponse> response =
                headAdminPendingReviewService.list(canGoOnline);

        return ResponseEntity.ok(
                HEADApiResponse.ok(response,"Listado obtenido correctamente")
        );
    }
}

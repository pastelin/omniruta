package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.controllers;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.entities.response.HEADMyRatingResponse;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.service.HEADStaffRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/staff")
public class HEADStaffRatingController {

    private final HEADStaffRatingService staffRatingService;

    @GetMapping("/my-rating")
    public ResponseEntity<HEADApiResponse<HEADMyRatingResponse>> getMyRating(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return ResponseEntity.ok(HEADApiResponse.ok(
                staffRatingService.getMyRatingForStaff(page, size)
        ));
    }
}
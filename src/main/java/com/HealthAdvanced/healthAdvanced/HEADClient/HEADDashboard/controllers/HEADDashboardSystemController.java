package com.HealthAdvanced.healthAdvanced.HEADClient.HEADDashboard.controllers;

import com.HealthAdvanced.healthAdvanced.HEADPromotions.service.HEADPromotionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard/v1")
public class HEADDashboardSystemController {
    @Autowired
    private HEADPromotionsService headPromotionsService;

    @GetMapping("/services/available")
    public ResponseEntity<?> servicesAvailable(@RequestParam(required=false) Double lat,
                                               @RequestParam(required=false) Double lng,
                                               @RequestParam(required=false, defaultValue="5.0") Double radiusKm) {
        return new ResponseEntity<>(headPromotionsService.getDashboardCards(lat,lng, radiusKm), HttpStatus.OK);
    }
}

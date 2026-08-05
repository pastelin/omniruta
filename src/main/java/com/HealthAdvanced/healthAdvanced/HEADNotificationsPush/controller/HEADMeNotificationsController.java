package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service.HEADNotificationsApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/notifications")
@RequiredArgsConstructor
public class HEADMeNotificationsController {

    private final HEADNotificationsApiService service;

    @GetMapping
    public HEADApiResponse<HEADNotificationsResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return HEADApiResponse.ok(service.list(page, size));
    }

    @GetMapping("/summary")
    public HEADApiResponse<HEADNotificationsResponse.Summary> summary() {
        return HEADApiResponse.ok(service.summary());
    }

    @PostMapping("/{id}/read")
    public HEADApiResponse<Void> read(@PathVariable long id) {
        service.markRead(id);
        return HEADApiResponse.ok(null, "OK");
    }

    @PostMapping("/read-all")
    public HEADApiResponse<Void> readAll() {
        service.markAllRead();
        return HEADApiResponse.ok(null, "OK");
    }

    @DeleteMapping("/{id}")
    public HEADApiResponse<Void> delete(@PathVariable long id) {
        service.delete(id);
        return HEADApiResponse.ok(null, "OK");
    }
}
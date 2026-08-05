package com.HealthAdvanced.healthAdvanced.HEADSchedule.entity.response;

import java.time.LocalDate;
import java.util.List;

public record HEADDaySlots(
        LocalDate date,
        List<HEADSlot> slots
) {}

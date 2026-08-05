package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class HEADFolioGen {
    public static String newFolio() {
        var ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        var rnd = UUID.randomUUID().toString().substring(0,6).toUpperCase();
        return ts + "-" + rnd; // ej: 20251124-143210-A1B2C3
    }
}
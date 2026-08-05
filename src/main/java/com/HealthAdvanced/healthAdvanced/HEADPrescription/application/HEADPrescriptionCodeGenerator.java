package com.HealthAdvanced.healthAdvanced.HEADPrescription.application;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class HEADPrescriptionCodeGenerator {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneOffset.UTC);

    public String nextCode() {
        // Ej: RX-20251224...-1234
        String ts = FMT.format(Instant.now());
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "RX-" + ts + "-" + rand;
    }
}

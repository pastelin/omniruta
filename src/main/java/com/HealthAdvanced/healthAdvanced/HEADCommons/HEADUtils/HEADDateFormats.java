package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class HEADDateFormats {

    private HEADDateFormats() {}

    private static final DateTimeFormatter HH_MM =
            DateTimeFormatter.ofPattern("HH:mm", new Locale("es", "MX"));

    /** Ej: es_MX -> "4:00 p. m." | en_US -> "4:00 PM" */
    public static String formatTime(Instant instant, ZoneId zoneId, Locale locale) {
        if (instant == null) return "";
        var zdt = instant.atZone(zoneId);

        // "SHORT" = hora corta con AM/PM según locale
        var fmt = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withLocale(locale);

        return fmt.format(zdt);
    }

    /** Si quieres mostrar también fecha: "4 ene 2026, 4:00 p. m." */
    public static String formatDateTime(Instant instant, ZoneId zoneId, Locale locale) {
        if (instant == null) return "";
        var zdt = instant.atZone(zoneId);

        var fmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(locale);

        return fmt.format(zdt);
    }

    public static Instant convertStrToInstantTz(String data) {
        return Instant.parse(data);
    }

    public static Instant convertStrToInstant(String data) {
        if (data == null || data.isBlank()) throw new IllegalArgumentException("scheduledTime required");

        ZoneId zone = ZoneId.of("America/Mexico_City");

        // ISO con zona (recomendado)
        try { return OffsetDateTime.parse(data).toInstant(); } catch (Exception ignored) {}

        // "yyyy-MM-dd HH:mm:ss"
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(data, f).atZone(zone).toInstant();
        } catch (Exception ignored) {}

        // "yyyy-MM-dd HH:mm"
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(data, f).atZone(zone).toInstant();
    }



    public static String formatDate(Instant date) {

        ZoneId zone = ZoneId.of("America/Mexico_City");
        Locale locale = new Locale("es", "MX");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", locale);
        String dateFormatted = fmt.format(date.atZone(zone));
        return dateFormatted.substring(0, 1).toUpperCase(locale) + dateFormatted.substring(1);
    }

    public static String buildTimeRange(Instant startInstant, ZoneId zone, Duration duration) {
        ZonedDateTime start = startInstant.atZone(zone);
        ZonedDateTime end = start.plus(duration);

        String startStr = start.format(HH_MM);
        String endStr = end.format(HH_MM);

        return startStr + " - " + endStr;
    }

    public static String formatDurationEs(int minutes) {
        if (minutes <= 0) return "0 min";

        if (minutes % 60 == 0) {
            long hours = minutes / 60;
            if (hours == 1) return "1 hora";
            return hours + " hrs";
        }

        return minutes + " min";
    }

}

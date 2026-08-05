package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADUtils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class HEADCommonsUtils {

    private HEADCommonsUtils() {}
    public static BCryptPasswordEncoder passwordEncoder;
    public static String getDateTimeCurrent(){
        LocalDateTime dateTimeCurrent = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeFormatter = dateTimeCurrent.format(formatter);
        return dateTimeFormatter;
    }

    public static LocalDateTime parseToDateTime(String dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }

    public static String generatorUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String setEncodeValue(String value) {
        return passwordEncoder.encode(value);
    }

    public static Boolean isMatchesPasswords(String rawPassword, String encodedPassword ) {
        return passwordEncoder.matches(rawPassword,encodedPassword);
    }

    public static byte[] sha256Bytes(String input) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String sha256Hex(String input) {
        return toHex(sha256Bytes(input));
    }

    public static String generatePin4() {
        int n = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 10000);
        return String.format("%04d", n);
    }

}

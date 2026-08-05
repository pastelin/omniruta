package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

public record HEADScheduleConfirmed(
        String professionalName,
        String professionalRole,
        String date,
        String dateFormatted,
        String time,
        String duration,
        String serviceType,
        String address,
        String price
) {}

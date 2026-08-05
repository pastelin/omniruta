package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.events;

public final class HEADJobEvents {
    private HEADJobEvents() {}
    public static final String JOB_UPDATE = "job:update";   // snapshot del job
    public static final String JOB_ERROR  = "job:error";    // opcional, para avisos
    public static String roomOf(Long jobId) { return "job:" + jobId; }
}
package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.Dtos;

public record HEADJobAcceptedVideo(Long jobId, String uuidUser) {

    public static HEADJobAcceptedVideo acceptedVideo(Long jobId, String otherUuidUser) {
        return new HEADJobAcceptedVideo(jobId,otherUuidUser);
    }
}

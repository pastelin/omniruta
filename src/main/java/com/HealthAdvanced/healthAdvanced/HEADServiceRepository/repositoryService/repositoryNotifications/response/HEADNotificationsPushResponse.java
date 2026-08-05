package com.HealthAdvanced.healthAdvanced.HEADServiceRepository.repositoryService.repositoryNotifications.response;

import java.util.ArrayList;


public class HEADNotificationsPushResponse {
    private float multicast_id;
    private float success;
    private float failure;
    private float canonical_ids;

    private ArrayList<HEADNotificationPushResults> results;


    public ArrayList<HEADNotificationPushResults> getResults() {
        return results;
    }

    public void setResults(ArrayList<HEADNotificationPushResults> results) {
        this.results = results;
    }

    public float getMulticast_id() {
        return multicast_id;
    }

    public float getSuccess() {
        return success;
    }

    public float getFailure() {
        return failure;
    }

    public float getCanonical_ids() {
        return canonical_ids;
    }

    // Setter Methods

    public void setMulticast_id( float multicast_id ) {
        this.multicast_id = multicast_id;
    }

    public void setSuccess( float success ) {
        this.success = success;
    }

    public void setFailure( float failure ) {
        this.failure = failure;
    }

    public void setCanonical_ids( float canonical_ids ) {
        this.canonical_ids = canonical_ids;
    }
}

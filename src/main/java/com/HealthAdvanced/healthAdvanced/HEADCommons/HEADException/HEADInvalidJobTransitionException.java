package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.HEADJobState;

public class HEADInvalidJobTransitionException extends RuntimeException {
    public HEADInvalidJobTransitionException(Long jobId, HEADJobState from, HEADJobState to, String msg) {
        super("Invalid transition jobId=" + jobId + " from=" + from + " to=" + to + (msg != null ? " :: " + msg : ""));
    }
}
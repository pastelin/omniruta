package com.HealthAdvanced.healthAdvanced.HEADClient.HEADAddress.intefaces;

import java.time.Instant;

public interface HEADLocationAggView {
    String getAddress();
    long getTimesUsed();
    Instant getLastUsedAt();
}

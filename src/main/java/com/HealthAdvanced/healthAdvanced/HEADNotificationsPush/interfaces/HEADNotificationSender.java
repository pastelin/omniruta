package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;

public interface HEADNotificationSender {
    void send(HEADNotificationCommand command);
}


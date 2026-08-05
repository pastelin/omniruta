package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.interfaces;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationText;

public interface HEADNotificationTextComposer {
    HEADNotificationText compose(HEADNotificationCommand command);
}


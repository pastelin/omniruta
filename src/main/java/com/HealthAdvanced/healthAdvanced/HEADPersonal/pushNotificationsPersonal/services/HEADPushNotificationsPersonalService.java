package com.HealthAdvanced.healthAdvanced.HEADPersonal.pushNotificationsPersonal.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.commons.interfaces.HEADWsEmitter;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.pushNotificationsPersonal.entity.HEADRequestServiceNotificationPush;
import com.HealthAdvanced.healthAdvanced.HEADServiceRepository.constantsServices.HEADServiceNotificationsConstants;

import org.springframework.stereotype.Service;
import static com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWsEvents.SEND_NOTIFICATION_PERSONAL_ACTION;

@Service
@lombok.RequiredArgsConstructor
public class HEADPushNotificationsPersonalService implements IHEADPushNotificationsPersonalService {

    private final HEADWsEmitter emitter;

    @Override
    public void sendNotificationPushPersonal(HEADWebSocketUsersEntity wsPersonal,
                                             HEADActivePersonal headClient,
                                             HEADActiveLocationPersonal headActiveLocationPersonal) {
        // Notificación en tiempo real por WS (room del personal)
        var push = new HEADRequestServiceNotificationPush();
        push.setTitle(HEADServiceNotificationsConstants.titleRequestService);
        push.setBody(HEADServiceNotificationsConstants.bodyRequestService);
        push.setUuIdClient(headClient.getUuIdClient());
        push.setDistance(wsPersonal.getDistanceMts());

        // Room del personal = "user:{uuidPersonal}"
        emitter.toUser(wsPersonal.getUuIdPersonal(), SEND_NOTIFICATION_PERSONAL_ACTION, push);

        // Si además tienes push FCM/Expo, aquí dispara el HTTP a FCM con su deviceToken (no mostrado).
    }
}


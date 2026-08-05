package com.HealthAdvanced.healthAdvanced.HEADPersonal.pushNotificationsPersonal.services;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActiveLocationPersonal;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import com.corundumstudio.socketio.SocketIOClient;

import java.util.List;

public interface IHEADPushNotificationsPersonalService {
    void sendNotificationPushPersonal(HEADWebSocketUsersEntity wsPersonal,
                                             HEADActivePersonal headClient,
                                             HEADActiveLocationPersonal headActiveLocationPersonal);
}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.HEADWebSocketHandler;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADWebSocketUsersEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Dto.Response.HEADWebSocketEntity;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.Geolocalization.Service.HEADActiveLocationPersonalService;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services.HEADPersonalService;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


public class HEADMapPersonalController {


}

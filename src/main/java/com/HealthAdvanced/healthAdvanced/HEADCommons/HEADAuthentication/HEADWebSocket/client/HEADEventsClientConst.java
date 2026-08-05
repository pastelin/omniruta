package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.client;

public class HEADEventsClientConst {
    public static final String NS_PATH_CLIENT = "/client";
    public static final String CLIENT_HEARTBEAT    = "HEARTBEAT";
    public static final String CLIENT_REAUTH       = "reauth";
    public static final String CLIENT_LOC_UPDATE   = "loc:update_client";
    public static final String CLIENT_ROOM_PREFIX = "client:";
    public static final String CLIENT_STATE        = "state";
    public final static String REQUEST_SERVICE_CLIENT = "requestServiceClient";
    public static final String ACK_CLIENT_OK = "client_ok";
    public static final String CLIENT_SCHEDULE_PENDING = "CLIENT_SCHEDULE_PENDING";
    public static final String CLIENT_OFFER_SCHEDULE = "CLIENT_OFFER_SCHEDULE";
    public static final String CLIENT_OFFER_SCHEDULE_SELECT = "CLIENT_OFFER_SCHEDULE_SELECT";
    public static final String CLIENT_REQUEST_SCHEDULE_PROPOSAL = "CLIENT_REQUEST_SCHEDULE_PROPOSAL";
    public static final String JOB_CANCEL_CLIENT = "JOB_CANCEL_CLIENT";
    public static final String JOB_ARRIVAL_PIN_ISSUED = "JOB_ARRIVAL_PIN_ISSUED";
    public static final String JOB_SEARCH_STAFF = "JOB_SEARCH_STAFF";
}

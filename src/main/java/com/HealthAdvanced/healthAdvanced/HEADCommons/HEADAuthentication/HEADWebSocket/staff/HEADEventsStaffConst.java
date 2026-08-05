package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff;

public class HEADEventsStaffConst {
    public static final String NS_PATH = "/staff";

    // Rooms
    public static final String STAFF_ROOM_PREFIX = "staff:"; // p.ej. staff:UUID

    // Staff lifecycle / estado
    public static final String STAFF_REAUTH       = "reauth";
    public static final String STAFF_HEARTBEAT    = "HEARTBEAT";
    public static final String STAFF_ONLINE_TOGGLE= "online:toggle";
    public static final String STAFF_LOC_UPDATE   = "loc:update";
    public static final String STAFF_STATE        = "state";          // push de estado del staff

    // Flujo de trabajo
    public static final String JOB_ACCEPT   = "JOB_ACCEPT";
    public static final String JOB_ARRIVED  = "JOB_ARRIVED";
    public static final String JOB_STARTED    = "JOB_STARTED";
    public static final String JOB_COMPLETED = "JOB_COMPLETED";
    public static final String JOB_CANCEL   = "JOB_CANCEL";
    public static final String JOB_CANCEL_STAFF = "JOB_CANCEL_STAFF";
    public static final String JOB_STATE    = "JOB_STATE";   // broadcast de estado del job
    public static final String EVENT_JOB_OFFER = "EVENT_JOB_OFFER";
    public static final String EVENT_CLIENT_UPDATE = "EVENT_CLIENT_UPDATE";
    public static final String NEARBY_DELTA = "NEARBY_DELTA";

    public static final String JOB_STARTED_PIN = "JOB_STARTED_PIN";

    // Acks
    public static final String ACK_OK = "ok";
}

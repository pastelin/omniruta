package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket;

public class HEADWsEvents {
    public final static String SEND_NOTIFICATION_TO_CLIENT = "sendNotificationToClient";
    public final static String SEND_PERSONAL_FOUND = "sendPersonalFound";

    public static final String HEARTBEAT = "HEARTBEAT";
    public static final String REAUTH = "REAUTH";
    public final static String ERROR_RESPONSE_EVENT = "ERROR_RESPONSE_EVENT";
    public final static String PERSONAL_AVAILABLE_CLIENT = "getPersonalsAvailableClient";
    public final static String UPDATE_LOCATION_PERSONAL = "updateLocationPersonal";
    public final static String IS_ACCEPTED_PERSONAL_TO_CLIENT = "isAcceptedPersonalToClient";
    public final static String UPDATE_LOCATION_CLIENT_CURRENT = "updateLocationClientCurrent";

    //Response

    public final static String PERSONAL_AVAILABLE_RESPONSE = "personalAvailableResponse";
    public final static String PERSONAL_REMOVE_RESPONSE = "personalRemoveResponse";

    public final static String SEND_NOTIFICATION_PERSONAL_ACTION = "pushNotificationActionPersonal";
    public final static String STAFF_VERIFICATION_UPDATED = "STAFF_VERIFICATION_UPDATED";
    public final static String STAFF_COMPLETED_SUCCESS = "STAFF_COMPLETED_SUCCESS";
    public final static String CLIENT_LOCATION_SAVED = "CLIENT_LOCATION_SAVED";
    public final static String ASSIGNMENT_STARTED = "ASSIGNMENT_STARTED";
    public final static String ASSIGNMENT_FAILED = "ASSIGNMENT_FAILED";
    public final static String NEARBY_SUBSCRIBE   = "NEARBY_SUBSCRIBE";
    public final static String NEARBY_UNSUBSCRIBE = "NEARBY_UNSUBSCRIBE";
    public final static String NEARBY_SNAPSHOT    = "NEARBY_SNAPSHOT";
    public final static String NEARBY_DELTA       = "NEARBY_DELTA";
    public final static String EVENT_JOB_OFFER     = "JOB_OFFERED";
    public final static String JOB_STATE_CHANGED   = "JOB_STATE_CHANGED";
    public final static String ROUTE_TO_CLIENT = "ROUTE_TO_CLIENT";
    public final static String UNSUBSCRIBED = "UNSUBSCRIBED";
    public final static String PRESENCE_UPDATE_ACK = "PRESENCE_UPDATE_ACK";
    public final static String PRESENCE_UPDATE = "PRESENCE_UPDATE";
    public final static String VIDEO_TO_CLIENT = "VIDEO_TO_CLIENT";
    public final static String STAFF_RATING_SUMMARY_REQ = "STAFF_RATING_SUMMARY_REQ";
    public final static String STAFF_SCHEDULE_PENDING = "STAFF_SCHEDULE_PENDING";
    public final static String STAFF_OFFER_SCHEDULE_PROPOSE = "STAFF_OFFER_SCHEDULE_PROPOSE";
    public final static String JOB_SCHEDULE_CONFIRMED = "JOB_SCHEDULE_CONFIRMED";
    public final static String CLIENT_OFFER_SCHEDULE_DECLINE = "CLIENT_OFFER_SCHEDULE_DECLINE";
    public final static String JOB_ACCEPTED_AWAITING_START = "JOB_ACCEPTED_AWAITING_START";
    public final static String DECISION_OPENED = "DECISION_OPENED";
}
package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventsName;

public interface HEADChatWsEvents {

    // Core mensajes
    String CHAT_SEND_MESSAGE          = "CHAT_SEND_MESSAGE";
    String CHAT_MESSAGE               = "CHAT_MESSAGE";

    // Estados (delivered / read)
    String CHAT_MARK_DELIVERED        = "CHAT_MARK_DELIVERED";
    String CHAT_MARK_READ             = "CHAT_MARK_READ";
    String CHAT_MESSAGE_STATUS_UPDATE = "CHAT_MESSAGE_STATUS_UPDATE";

    // Historial / no leídos
    String CHAT_HISTORY_REQUEST       = "CHAT_HISTORY_REQUEST";
    String CHAT_HISTORY_RESPONSE      = "CHAT_HISTORY_RESPONSE";
    String CHAT_UNREAD_SUMMARY_REQ    = "CHAT_UNREAD_SUMMARY_REQ";
    String CHAT_UNREAD_SUMMARY        = "CHAT_UNREAD_SUMMARY";

    // Typing
    String CHAT_TYPING                = "CHAT_TYPING";
    String CHAT_TYPING_UPDATE         = "CHAT_TYPING_UPDATE";

    String CHAT_ACTIVE                = "CHAT_ACTIVE";
    String CHAT_INACTIVE              = "CHAT_INACTIVE";

    String CHAT_MESSAGE_DELETE        = "CHAT_MESSAGE_DELETE";
    String CHAT_MESSAGE_DELETED       = "CHAT_MESSAGE_DELETED";

    String CHAT_PRESENCE_UPDATED        = "CHAT_PRESENCE_UPDATED";
}


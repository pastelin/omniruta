package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.exceptions;

public class HEADFcmSendException extends RuntimeException {
    private final int httpCode;
    private final String status;     // NOT_FOUND, INVALID_ARGUMENT...
    private final String errorCode;  // UNREGISTERED (lo más útil)
    public HEADFcmSendException(int httpCode, String status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpCode = httpCode;
        this.status = status;
        this.errorCode = errorCode;
    }
    public int httpCode() { return httpCode; }
    public String status() { return status; }
    public String errorCode() { return errorCode; }
}

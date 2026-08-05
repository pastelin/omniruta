package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class HEADBadRequestException extends RuntimeException {
    public HEADBadRequestException(String message) {
        super(message);
    }
}

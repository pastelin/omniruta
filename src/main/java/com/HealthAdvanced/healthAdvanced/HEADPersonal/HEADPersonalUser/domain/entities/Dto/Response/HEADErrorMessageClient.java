package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Response;

public class HEADErrorMessageClient extends Throwable {
    private String messageError;

    public HEADErrorMessageClient(String messageError) {
        this.setMessageError(messageError);
    }
    public String getMessageError() {
        return messageError;
    }

    public void setMessageError(String messageError) {
        this.messageError = messageError;
    }
}

package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
public class HEADClientRegisterRequestDto {
    private String nameClient;
    private String lastName;
    private String firstName;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String password;
    private String numberPhone;
    private Long sexClient;
    private Long termsDocumentId;
    private Long privacyDocumentId;
    private String language;
}

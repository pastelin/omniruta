package com.HealthAdvanced.healthAdvanced.HEADClient.headClient.Entity.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
public class HEADClientRegisterResponseInfoDto {
    private String nameClient;
    private String lastName;
    private String firstName;
    private String email;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String numberPhone;
}

package com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.api.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HEADCreateConsultingLeadRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 160)
    private String email;

    @Size(max = 30)
    private String phone;

    @NotBlank
    @Size(max = 160)
    private String projectType;

    @NotBlank
    @Size(max = 3000)
    private String message;

    @NotBlank
    @Size(max = 300)
    private String company;

    @NotBlank
    @Size(max = 200)
    private String budget;

}

package com.HealthAdvanced.healthAdvanced.HEADPrescription.controller;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADApiResponse;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.enums.HEADMedicationForm;
import com.HealthAdvanced.healthAdvanced.HEADPrescription.domain.model.response.HEADMedicationFormItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/staff/prescriptions")
@RequiredArgsConstructor
public class HEADPrescriptionStaffController {

    @GetMapping("/medication-forms")
    public HEADApiResponse<List<HEADMedicationFormItem>> medicationForms() {

        List<HEADMedicationFormItem> items = Arrays.stream(HEADMedicationForm.values())
                .map(f -> new HEADMedicationFormItem(
                        f.name(),
                        f.getLabelEs(),
                        f.getEmoji()
                ))
                .toList();

        return HEADApiResponse.ok(items);
    }
}

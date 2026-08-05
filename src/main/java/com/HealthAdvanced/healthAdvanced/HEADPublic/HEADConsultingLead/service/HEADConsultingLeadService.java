package com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.service;

import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.api.request.HEADCreateConsultingLeadRequest;
import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.entity.HEADConsultingLead;
import com.HealthAdvanced.healthAdvanced.HEADPublic.HEADConsultingLead.repository.HEADConsultingLeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADConsultingLeadService {

    private final HEADConsultingLeadRepository repository;

    @Transactional
    public HEADConsultingLead createLead(HEADCreateConsultingLeadRequest request) {
        HEADConsultingLead lead = new HEADConsultingLead();
        lead.setName(request.getName().trim());
        lead.setEmail(request.getEmail().trim().toLowerCase());
        lead.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        lead.setProjectType(request.getProjectType().trim());
        lead.setMessage(request.getMessage().trim());
        lead.setCompany(request.getCompany().trim());
        lead.setBudget(request.getBudget().trim());
        lead.setSource("consultoria_web");
        lead.setStatus("NEW");

        return repository.save(lead);
    }
}

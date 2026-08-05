package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.servicesMap;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADActivePersonalDTO;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADActivePersonal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HEADActivePersonalMapService {
    public HEADActivePersonal activePersonal(HEADActivePersonalDTO activePersonalDTO) {
        return new HEADActivePersonal(activePersonalDTO);
    }

    public  HEADActivePersonalDTO activePersonalDTO(HEADActivePersonal activePersonal) {
        return new HEADActivePersonalDTO(activePersonal);
    }

    public List<HEADActivePersonalDTO> activePersonalDTOList(List<HEADActivePersonal> listActivePersonal) {
        List<HEADActivePersonalDTO> activePersonalDTOS = new ArrayList<>();
        listActivePersonal.stream().forEach(
                activePersonal -> {
                    activePersonalDTOS.add(activePersonalDTO(activePersonal));
                }
        );
        return activePersonalDTOS;
    }
}

package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.services;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.repositories.HEADAuthRefreshTokenRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.interfaces.HEADPresenceStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADWebSocket.staff.HEADStaffStateStore;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HEADDeleteStaffAccountService {

    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADAuthRefreshTokenRepository refreshTokenRepository;
    private final HEADPresenceStore presenceStore;
    private final HEADStaffStateStore staffStateStore;
    private final PasswordEncoder passwordEncoder;
    private final HEADJwtGenerator jwtGenerator;

    @Transactional
    public void deleteStaffAccount() {
        var staffUuid = jwtGenerator.getUserNamePersonalUser();
        HEADPersonalUser staff = personalUserRepository.findByUidUser(staffUuid)
                .orElseThrow(() -> new EntityNotFoundException("Personal no encontrado: " + staffUuid));

        Long staffId = staff.getIdUser();
        String oldUuid = staff.getUidUser();

        refreshTokenRepository.revokeAllByUserId(staffId);

        for (String sessionId : presenceStore.sessionIdsFor(oldUuid)) {
            presenceStore.remove(sessionId);
        }

        staffStateStore.clear(oldUuid);

        anonymizeStaff(staff);

        personalUserRepository.save(staff);
    }

    private void anonymizeStaff(HEADPersonalUser staff) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        staff.setNombre("DELETED");
        staff.setAPaterno("STAFF");
        staff.setAMaterno(suffix);
        staff.setFechaNacimiento(null);
        staff.setTelefono(String.valueOf(System.currentTimeMillis()).substring(3, 13));
        staff.setEmail("deleted+staff+" + suffix.toLowerCase() + "@docarya.invalid");
        staff.setPassword(passwordEncoder.encode("DELETED-" + UUID.randomUUID() + "-" + System.nanoTime()));
        staff.setIdSexUser(null);
        staff.setGoogleSub(null);
        staff.setAuthProvider(HEADAuthProvider.LOCAL);
        staff.setRoles("DELETED_ACCOUNT");
        staff.setIsEnabled(Boolean.FALSE);

        // opcional
        // staff.setUidUser("deleted-" + UUID.randomUUID());
    }
}
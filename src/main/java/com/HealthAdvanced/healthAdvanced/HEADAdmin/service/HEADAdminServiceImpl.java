package com.HealthAdvanced.healthAdvanced.HEADAdmin.service;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.contracts.HEADAdminService;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.entity.HEADAdmin;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminCreateRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.request.HEADAdminLoginRequest;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminLoginResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.model.response.HEADAdminResponse;
import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.repository.HEADAdminRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.service.HEADAuthService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HEADAdminServiceImpl implements HEADAdminService {

    private final HEADAdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final HEADAuthService authService;

    @Override
    public HEADAdminResponse createAdmin(HEADAdminCreateRequest request) {
        if (adminRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new HEADBadRequestException("Ya existe un admin con ese correo");
        }

        HEADAdmin admin = HEADAdmin.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(Boolean.TRUE)
                .build();

        admin = adminRepository.save(admin);
        return toResponse(admin);
    }

    @Override
    public HEADAdminLoginResponse login(HEADAdminLoginRequest request) {
        HEADAdmin admin = adminRepository.findByEmailIgnoreCaseAndActiveTrue(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new HEADBadRequestException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new HEADBadRequestException("Credenciales inválidas");
        }

        admin.setLastLoginAt(Instant.now());
        adminRepository.save(admin);

        var accessToken = authService.login(admin.getUidAdmin());

        return HEADAdminLoginResponse.builder()
                .accessToken(accessToken.getAccessToken())
                .refreshToken(accessToken.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(accessToken.getAccessExpiresAt())
                .admin(toResponse(admin))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HEADAdminResponse> findAll() {
        return adminRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HEADAdminResponse toResponse(HEADAdmin admin) {
        return HEADAdminResponse.builder()
                .id(admin.getId())
                .uidAdmin(admin.getUidAdmin())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .active(admin.getActive())
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
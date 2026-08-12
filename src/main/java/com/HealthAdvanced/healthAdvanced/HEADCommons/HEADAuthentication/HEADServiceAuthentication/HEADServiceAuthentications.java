package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication;

import com.HealthAdvanced.healthAdvanced.HEADAdmin.domain.repository.HEADAdminRepository;
import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.enums.HEADTypeUser;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADServiceAuthentication.entity.HEADUserInfo;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
public class HEADServiceAuthentications {
    @Autowired
    private HEADPersonalUserRepository personaUserRepository;
    @Autowired
    private HEADClientsRepository headClientsRepository;
    @Autowired
    private HEADAdminRepository headAdminRepository;

    /**
     * Convierte un string CSV de roles en un array sin prefijos extra.
     * Ej: "REGISTER_CLIENT,ACCESS_CLIENT" -> ["REGISTER_CLIENT","ACCESS_CLIENT"]
     */
    private String[] splitRoles(String rolesCsv) {
        return Arrays.stream(Objects.toString(rolesCsv, "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    public UserDetails loadUserOrClientByUsername(String username) {
        var user = this.personaUserRepository.findByUidUser(username).orElse(null);
        if (user != null) {
            String password = user.getPassword() != null ? user.getPassword() : "{noop}GOOGLE_USER";
            return User
                    .withUsername(username)
                    .password(password)
                    .roles(splitRoles(user.getRoles()))
                    .build();
        }

        var client = headClientsRepository.findByUuIdUser(username).orElse(null);
        if (client != null) {
            String password = client.getPassword() != null ? client.getPassword() : "{noop}GOOGLE_CLIENT";
            return User
                    .withUsername(username)
                    .password(password)
                    .roles(splitRoles(client.getRoles()))
                    .build();
        }

        var admin = headAdminRepository.findByUidAdmin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin no encontrado"));

        return User
                .withUsername(admin.getUidAdmin())
                .password(admin.getPasswordHash())
                .roles(admin.getRole().name())
                .build();
    }

    public HEADUserInfo loadUserOrClient(String username) {
        var user = this.personaUserRepository.findByUidUser(username).orElse(null);
        if (user != null) {
            return new HEADUserInfo(
                    username,
                    user.getIdUser(),
                    HEADTypeUser.USERS
            );


        }
        var client = headClientsRepository.findByUuIdUser(username).orElse(null);
        if (client != null) {
            return new HEADUserInfo(
                    username,
                    client.getIdUser(),
                    HEADTypeUser.USERS
            );
        }

        var admin = headAdminRepository.findByUidAdmin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin no encontrado"));
        return new HEADUserInfo(
                username,
                admin.getId(),
                HEADTypeUser.ADMIN
        );
    }
}

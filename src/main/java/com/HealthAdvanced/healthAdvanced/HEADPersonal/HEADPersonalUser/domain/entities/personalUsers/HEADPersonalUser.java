package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.enums.HEADAuthProvider;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.Dto.Request.HEADPersonalUserDTO;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADSexUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "personalUser")
public class HEADPersonalUser{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String uidUser;
    private String nombre;
    private String aPaterno;
    private String aMaterno;
    private LocalDate fechaNacimiento;
    private String telefono;
    private Boolean isEnabled;
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    private String roles;
    @ManyToOne
    private HEADSexUser idSexUser;
    @OneToMany(mappedBy = "idPersonalUser", fetch = FetchType.LAZY)
    private List<HEHOOccupationPersonalUser> occupationLinks = new ArrayList<>();
    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private HEADAuthProvider authProvider = HEADAuthProvider.LOCAL;
    @Column(name = "google_sub", unique = true)
    private String googleSub;

    public HEADPersonalUser(HEADPersonalUserDTO personalUserDTO, HEADSexUser sexUser) {
        this.uidUser = personalUserDTO.getUidUser();
        this.nombre = personalUserDTO.getNombre();
        this.aPaterno = personalUserDTO.getPaterno();
        this.aMaterno = personalUserDTO.getMaterno();
        this.fechaNacimiento = personalUserDTO.getFechaNacimiento();
        this.telefono = personalUserDTO.getTelefono();
        this.email = personalUserDTO.getEmail();
        this.password = personalUserDTO.getPassword();
        this.isEnabled = personalUserDTO.getIsAccepted();
        this.idSexUser = sexUser;
        this.roles = personalUserDTO.getRoles();
    }

}

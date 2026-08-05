package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.models;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_staff_professional_profile")
public class HEADStaffProfessionalProfile {

    @Id
    @Column(name = "staff_user_id")
    private Long staffUserId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser staffUser;

    @Column(name = "location_label", length = 120)
    private String locationLabel;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;
}
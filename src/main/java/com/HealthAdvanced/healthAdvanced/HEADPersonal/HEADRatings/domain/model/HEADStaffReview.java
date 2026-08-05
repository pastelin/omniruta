package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Data
@NoArgsConstructor
@Entity
@Table(
        name = "head_staff_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_job", columnNames = {"job_id"})
        },
        indexes = {
                @Index(name="idx_review_staff", columnList="staff_user_id"),
                @Index(name="idx_review_client", columnList="client_user_id"),
                @Index(name="idx_review_job", columnList="job_id"),
                @Index(name="idx_review_created", columnList="created_at")
        }
)
public class HEADStaffReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false, unique = true)
    private HEADJob job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser idPersonalUser;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_user_id", nullable = false)
    private HEADClients idUserClient;

    @Column(nullable = false)
    private Integer rating; // 1..5

    @Column(length = 500)
    private String comment;

    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }
}



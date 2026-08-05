package com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model;

import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(
        name = "head_staff_rating_summary",
        indexes = {
                @Index(name = "idx_summary_score", columnList = "bayesian_score"),
                @Index(name = "idx_summary_updated", columnList = "updated_at")
        }
)
public class HEADStaffRatingSummary {

    @Id
    @Column(name = "staff_user_id", nullable = false, insertable = false, updatable = false)
    private Long staffUserId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    private HEADPersonalUser idPersonalUser;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews = 0;

    @Column(name = "sum_rating", nullable = false)
    private Integer sumRating = 0;

    @Column(name = "avg_rating", precision = 4, scale = 2, nullable = false)
    private BigDecimal avgRating;

    @Column(name = "bayesian_score", precision = 4, scale = 2, nullable = false)
    private BigDecimal bayesianScore;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() { this.updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }
}


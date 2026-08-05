package com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model;

import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.enums.*;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.entities.personalUsers.HEADPersonalUser;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.domain.model.HEADStaffReview;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADRatings.enums.HEADReviewState;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADClients;
import com.HealthAdvanced.healthAdvanced.ModelsBD.Users.HEADServiceRequestClient;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "head_job",
        indexes = {
                @Index(name="idx_job_staff_state", columnList="staff_user_id,state,updated_at"),
                @Index(name="idx_job_request", columnList="request_id"),
                @Index(name="idx_job_client", columnList="client_id"),
                @Index(name="idx_job_sched", columnList="scheduled_at"),
                @Index(name="idx_job_payment", columnList="payment_status")
        })
public class HEADJob {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private HEADServiceRequestClient request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private HEADClients client;

    // Puede ser null si el job está OFFERED y aún no hay staff asignado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_user_id")
    private HEADPersonalUser staffUser;

    @Column(name = "staff_uuid", length = 64)
    private String staffUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private HEADJobState state;

    // Tiempos clave
    private Instant assignedAt;     // cuando se “ofrece”/asigna
    private Instant acceptedAt;
    private Instant arrivedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    // ---- PIN de inicio (ARRIVED -> STARTED) ----
    @Column(name = "arrival_pin_hash", length = 64)
    private String arrivalPinHash;

    @Column(name = "arrival_pin_attempts")
    private Integer arrivalPinAttempts = 0;

    @Column(name = "arrival_pin_created_at")
    private Instant arrivalPinCreatedAt;

    @Column(name = "arrival_pin_verified_at")
    private Instant arrivalPinVerifiedAt;


    // Programación/expiración de oferta
    @Column(name = "scheduled_at")
    private Instant scheduledAt;    // si fue programado
    private Instant offerExpiresAt; // fin de countdown de la oferta

    // Cancelación tipada
    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private HEADCancelledBy cancelledBy; // CLIENT / STAFF / SYSTEM

    @Enumerated(EnumType.STRING)
    @Column(length = 24)
    private HEADCancelReason cancelReason;

    @Column(length = 255)
    private String cancelNote;

    // Pago (opcional pero útil)
    @Column(precision = 10, scale = 2)
    private BigDecimal amount; // total cobrado al cliente

    @Column(precision = 10, scale = 2)
    private BigDecimal staffPayoutAmount; // neto al staff

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private HEADPaymentStatus paymentStatus;
    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "status_raw")
    private String stripeStatusRaw;

    @Column(length = 64)
    private String paymentId;

    private Instant capturedAt;  // captura
    private Instant settledAt;   // liquidación

    // Métricas de distancia/tiempo estimadas o reales
    private Long distanceMeters;
    private Long durationSeconds;

    // Snapshot de ubicación del cliente (opcional)
    private Double clientLat;
    private Double clientLng;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    @PreUpdate
    void preUpdate() { this.updatedAt = Instant.now(); }

    private String startAddress;
    private String endAddress;

    private Double distanceKmBucket;
    private Integer durationMinBucket;

    @Column(name = "route_north_lat")
    private Double routeNorthLat;

    @Column(name = "route_east_lng")
    private Double routeEastLng;

    @Column(name = "route_south_lat")
    private Double routeSouthLat;

    @Column(name = "route_west_lng")
    private Double routeWestLng;

    @Enumerated(EnumType.STRING)
    private HEADServiceMode serviceMode = HEADServiceMode.HOME;

    @Column(length = 64)
    private String callId;

    @OneToOne(mappedBy = "job", fetch = FetchType.LAZY)
    private HEADStaffReview staffReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_state", nullable = false)
    private HEADReviewState reviewState = HEADReviewState.NOT_APPLICABLE;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "schedule_time")
    private Instant scheduledTime;

    @Column(name = "schedule_reminder_sent")
    private Boolean scheduleReminderSent = false;

    @Column(name = "schedule_reminder_sent_at")
    private Instant scheduleReminderSentAt;

    @Column(name = "service_duration_min")
    private Integer serviceDurationMin;

}


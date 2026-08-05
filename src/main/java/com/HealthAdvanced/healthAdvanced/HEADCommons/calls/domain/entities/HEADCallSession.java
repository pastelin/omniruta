package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.entities;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallContextType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallEndReason;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallState;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "head_call_session")
public class HEADCallSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="call_id", nullable=false, unique=true, length=64)
    private String callId; // UUID string

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=24)
    private HEADCallState state;

    @Enumerated(EnumType.STRING)
    @Column(name="context_type", nullable=false, length=16)
    private HEADCallContextType contextType;

    @Column(name="context_id", length=64)
    private String contextId;

    @Column(name="created_by_uuid", nullable=false, length=64)
    private String createdByUuid;

    @Enumerated(EnumType.STRING)
    @Column(name="created_by_type", nullable=false, length=16)
    private HEADChatParticipantType createdByType;

    @Enumerated(EnumType.STRING)
    @Column(name="end_reason", length=16)
    private HEADCallEndReason endReason;

    @Column(name="ended_by_uuid", length=64)
    private String endedByUuid;

    private Instant createdAt;
    private Instant ringingAt;
    private Instant acceptedAt;
    private Instant connectedAt;
    private Instant endedAt;

    @OneToMany(mappedBy="session", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<HEADCallParticipant> participants = new ArrayList<>();
}

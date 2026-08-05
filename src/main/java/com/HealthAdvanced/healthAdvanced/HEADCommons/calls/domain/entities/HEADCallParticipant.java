package com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.entities;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.calls.domain.enums.HEADCallParticipantRole;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="head_call_participant",
        indexes = {@Index(name="idx_call_participant_call", columnList="call_session_id"),
                @Index(name="idx_call_participant_user", columnList="user_uuid")})
public class HEADCallParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="call_session_id")
    private HEADCallSession session;

    @Column(name="user_uuid", nullable=false, length=64)
    private String userUuid;

    @Enumerated(EnumType.STRING)
    @Column(name="user_type", nullable=false, length=16)
    private HEADChatParticipantType userType;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private HEADCallParticipantRole role;

    private Instant joinedAt;
    private Instant leftAt;
    private Instant lastSeenAt;
}

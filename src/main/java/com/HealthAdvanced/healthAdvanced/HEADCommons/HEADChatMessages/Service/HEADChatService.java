package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.Service;

import com.HealthAdvanced.healthAdvanced.HEADClient.headClient.repository.HEADClientsRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.HEADChatSendMessageRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.HEADActiveUserChayRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.HEADChatHistoryRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.HEADChatTypingRequest;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.request.wsRequest.HEADConversationKey;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.HEADChatMessageDto;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.response.wsResponse.*;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatParticipantType;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.eventPublisher.HEADChatEventPublisher;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.interfaces.HEADChatUnreadSummary;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.map.HEADChatMap;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat.HEADChatActiveConversationStore;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.notificationsPushChat.HEADChatNotificationService;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.repositories.HEADChatMessageRepository;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADException.HEADBadRequestException;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.model.HEADJob;
import com.HealthAdvanced.healthAdvanced.HEADCommons.jobs.domain.service.HEADJobService;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.nameEvents.HEADNotificationTemplates;
import com.HealthAdvanced.healthAdvanced.HEADPersonal.HEADPersonalUser.domain.repositories.irepositories.HEADPersonalUserRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADDocumentsRepository.HEADFileAssetRepository;
import com.HealthAdvanced.healthAdvanced.HEHOModuleDocuments.HEADModelBD.HEADFileAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HEADChatService {

    private final HEADChatMessageRepository messageRepository;
    private final HEADPersonalUserRepository personalUserRepository;
    private final HEADClientsRepository headClientsRepository;
    private final HEADChatMap headChatMap;
    private final HEADJobService headJobService;
    private final HEADFileAssetRepository headFileAssetRepository;
    private final HEADChatEventPublisher chatEvents;
    private final HEADChatProfileService chatProfileService;
    private final HEADJwtGenerator jwt;
    private final HEADChatPresenceService chatPresenceService;
    private final HEADChatNotificationService notificationService;
    private final HEADChatActiveConversationStore headChatActiveConversationStore;

    Map<String, HEADChatUserProfileDto> cache = new HashMap<>();

    private HEADChatUserProfileDto profileOf(String uuid) {
        return cache.computeIfAbsent(uuid, chatProfileService::getProfile);
    }

    /**
     * Se llama cuando llega un mensaje desde el socket del usuario.
     */
    @Transactional
    public HEADChatMessageDto sendMessage(HEADChatSendMessageRequest req, String senderUuid) {

        String conversationId = (req.conversationId() != null && !req.conversationId().isBlank())
                ? req.conversationId()
                : buildConversationId(senderUuid, req.recipientUuid(), req.jobId());

        HEADJob headJob = (req.jobId() != null) ? headJobService.findById(req.jobId()) : null;

        HEADChatMessage message = headChatMap.newChatMessage(req, headJob, senderUuid, conversationId);

        message.setSenderType(detectType(senderUuid));
        message.setRecipientType(detectType(req.recipientUuid()));

        if (req.fileAssetId() != null) {
            HEADFileAsset asset = headFileAssetRepository.findById(req.fileAssetId())
                    .orElseThrow(() -> new HEADBadRequestException("FileAsset not found: " + req.fileAssetId()));
            message.setFileAsset(asset);
        }


        var urlAvatarRecipient = chatProfileService.getProfile(req.recipientUuid());
        var urlAvatarSender = chatProfileService.getProfile(senderUuid);
        HEADChatMessage saved = messageRepository.save(message);
        HEADChatMessageDto dto = headChatMap.toDto(saved, urlAvatarRecipient.avatarUrl(), urlAvatarSender.avatarUrl(), senderUuid);

        boolean inThisChat = headChatActiveConversationStore.isActiveIn(dto.recipientUuid(), conversationId);

        chatEvents.messageSent(dto);
        if (!inThisChat) {
            notificationService.notifyNewChatMessage(dto);
        }
        log.error("[sendMessage] emitUser jobId={} domain={} inThisChat={}",
                dto.jobId(), dto, inThisChat);
        return dto;
    }

    /**
     * Historial paginado (para “cargar más mensajes” tipo WhatsApp).
     */
    @Transactional(readOnly = true)
    public void getConversationHistory(String userUuid, HEADChatHistoryRequest req) {
        var pageable = PageRequest.of(req.page(), req.size());
        var page = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                req.conversationId(),
                pageable
        );

        List<HEADChatMessageDto> items = page.stream()
                .map(dto -> {
                    var urlAvatarRecipient = chatProfileService.getProfile(dto.getRecipientUuid());
                    var urlAvatarSender = chatProfileService.getProfile(dto.getSenderUuid());
                    return headChatMap.toDto(dto, urlAvatarRecipient.avatarUrl(), urlAvatarSender.avatarUrl(), userUuid);
                })
                .toList();

        var historyResponse = new HEADChatHistoryPageDto(
                req.conversationId(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                items
        );

        chatEvents.messagesHistory(userUuid, detectType(userUuid), historyResponse);
    }

    /**
     * Marcar mensajes como entregados para un receptor.
     */
    @Transactional
    public HEADChatMessageStatusUpdateDto markAsDelivered(String conversationId, String recipientUuid) {
        List<HEADChatMessage> pending = messageRepository
                .findByConversationIdAndRecipientUuidAndStatusNot(
                        conversationId,
                        recipientUuid,
                        HEADChatMessageStatus.DELIVERED
                );

        if (pending.isEmpty()) {
            return new HEADChatMessageStatusUpdateDto(conversationId, List.of(), HEADChatMessageStatus.DELIVERED, Instant.now());
        }

        Instant now = Instant.now();
        List<Long> ids = new ArrayList<>();

        pending.forEach(m -> {
            if (m.getStatus() == HEADChatMessageStatus.SENT) {
                m.setStatus(HEADChatMessageStatus.DELIVERED);
                m.setDeliveredAt(now);
                ids.add(m.getId());
            }
        });

        messageRepository.saveAll(pending);

        HEADChatMessageStatusUpdateDto update = new HEADChatMessageStatusUpdateDto(
                conversationId,
                ids,
                HEADChatMessageStatus.DELIVERED,
                now
        );
        var message = pending.stream().findFirst().orElse(null);
        chatEvents.messageStatusUpdated(update, message);

        return update;
    }

    /**
     * Marcar mensajes como leídos para un receptor.
     */
    @Transactional
    public HEADChatMessageStatusUpdateDto markAsRead(String conversationId, String recipientUuid) {
        List<HEADChatMessage> pending = messageRepository
                .findByConversationIdAndRecipientUuidAndStatusNot(
                        conversationId,
                        recipientUuid,
                        HEADChatMessageStatus.READ
                );

        if (pending.isEmpty()) {
            return new HEADChatMessageStatusUpdateDto(conversationId, List.of(), HEADChatMessageStatus.READ, Instant.now());
        }

        Instant now = Instant.now();
        List<Long> ids = new ArrayList<>();

        pending.forEach(m -> {
            if (m.getStatus() != HEADChatMessageStatus.READ) {
                m.setStatus(HEADChatMessageStatus.READ);
                m.setReadAt(now);
                ids.add(m.getId());
            }
        });
        messageRepository.saveAll(pending);


        HEADChatMessageStatusUpdateDto update = new HEADChatMessageStatusUpdateDto(
                conversationId,
                ids,
                HEADChatMessageStatus.READ,
                now
        );

        var message = pending.stream().findFirst().orElse(null);
        chatEvents.messageStatusUpdated(update, message);
        return update;
    }

    @Transactional(readOnly = true)
    public void handleTyping(HEADChatTypingRequest req, String fromUuid) {

        String conversationId = req.conversationId();

        // 1) Cargar SOLO el primer mensaje de la conversación
        var page = messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationId,
                PageRequest.of(0, 1)   // page 0, size 1
        );

        var firstOpt = page.getContent().stream().findFirst();
        if (firstOpt.isEmpty()) {
            log.warn("[handleTyping] no messages for conversationId={}", conversationId);
            return;
        }

        var first = firstOpt.get();

        // 2) Determinar el otro participante
        String otherUuid;
        if (Objects.equals(fromUuid, first.getSenderUuid())) {
            otherUuid = first.getRecipientUuid();
        } else if (Objects.equals(fromUuid, first.getRecipientUuid())) {
            otherUuid = first.getSenderUuid();
        } else {
            log.warn("[handleTyping] fromUuid={} is not participant of conversationId={}",
                    fromUuid, conversationId);
            return;
        }

        var typing = new HEADChatTypingUpdateDto(
                conversationId,
                fromUuid,
                req.isTyping()
        );

        chatEvents.typingUpdated(typing, otherUuid, detectType(otherUuid));
    }


    /**
     * Construye un conversationId estable, por ejemplo:
     * jobId|userA|userB (ordenando uuids para que sea simétrico).
     */
    public String buildConversationId(String userA, String userB, Long jobId) {
        String first = userA.compareTo(userB) < 0 ? userA : userB;
        String second = userA.compareTo(userB) < 0 ? userB : userA;
        return jobId + "|" + first + "|" + second;
    }

    public HEADChatParticipantType detectType(String uuIdUser) {
        var personalUser = personalUserRepository.findByUidUser(uuIdUser).orElse(null);
        if (personalUser != null) {
            return HEADChatParticipantType.STAFF;
        }

        var clientUser = headClientsRepository.findByUuIdUser(uuIdUser).orElse(null);
        if (clientUser != null) {
            return HEADChatParticipantType.CLIENT;
        }

        return HEADChatParticipantType.SYSTEM;
    }

    @Transactional(readOnly = true)
    public long getTotalUnread(String recipientUuid) {
        return messageRepository.countUnreadMessages(recipientUuid, HEADChatMessageStatus.READ);
    }

    @Transactional(readOnly = true)
    public List<HEADChatUnreadSummary> getUnreadByConversation(String recipientUuid) {
        return messageRepository.findUnreadByConversation(recipientUuid, HEADChatMessageStatus.READ);
    }


    @Transactional(readOnly = true)
    public void pushUnreadSummary(String userUuid) {
        long total = getTotalUnread(userUuid);
        List<HEADChatUnreadSummary> list = getUnreadByConversation(userUuid);
        chatEvents.unreadSummaryUpdated(userUuid, detectType(userUuid), list);
    }

    @Transactional(readOnly = true)
    public String buildConversationIdFromJob(Long jobId) {
        HEADJob job = headJobService.findById(jobId); // ya lo tienes
        String clientUuid = job.getClient().getUuIdUser();
        String staffUuid = job.getStaffUuid(); // o job.getStaffUser().getUuIdUser()

        return buildConversationId(clientUuid, staffUuid, jobId);
    }

    @Transactional(readOnly = true)
    public HEADChatOpenConversationDto openJobChat(Long jobId,
                                                   int page,
                                                   int size) {

        String callerUuid = jwt.getUserNamePersonalUser();
        log.info("[openJobChat] jobId={} callerUuid={}", jobId, callerUuid);
        HEADJob job = headJobService.findById(jobId);
        log.info("[openJobChat] job.clientUuid={} job.staffUuid={}",
                job.getClient().getUuIdUser(),
                job.getStaffUuid());
        String clientUuid = job.getClient().getUuIdUser();
        String staffUuid = job.getStaffUuid();

        // 1) Seguridad: caller debe ser client o staff del job
        if (!callerUuid.equals(clientUuid) && !callerUuid.equals(staffUuid)) {
            log.warn("[openJobChat] FORBIDDEN jobId={} callerUuid={} isClient={} isStaff={}",
                    jobId, callerUuid, clientUuid, staffUuid);
            throw new HEADBadRequestException("No tienes acceso al chat de este servicio");
        }

        // 2) ConversationId estable basado en jobId + uuids
        String conversationId = buildConversationId(clientUuid, staffUuid, jobId);

        // 3) Historial paginado
        var pageable = PageRequest.of(page, size);
        var pageMessages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);

        // 4) Tipos de caller y "otro"
        HEADChatParticipantType callerType = detectType(callerUuid);
        String otherUuid = callerUuid.equals(clientUuid) ? staffUuid : clientUuid;
        HEADChatParticipantType otherType = detectType(otherUuid);

        // 5) Perfil del otro usuario (nombre, avatar)
        HEADChatUserProfileDto otherProfile = chatProfileService.getProfile(otherUuid);

        var items = pageMessages.stream()
                .map(dto -> {
                    var recipientProfile = profileOf(dto.getRecipientUuid());
                    var senderProfile = profileOf(dto.getSenderUuid());
                    return headChatMap.toDto(
                            dto,
                            recipientProfile != null ? recipientProfile.avatarUrl() : null,
                            senderProfile != null ? senderProfile.avatarUrl() : null,
                            callerUuid
                    );
                })
                .toList();
        log.info("[openJobChat] items={} otherProfile={}", items, otherProfile);

        HEADChatParticipantType otherUserType = (otherType == HEADChatParticipantType.STAFF)
                ? HEADChatParticipantType.STAFF
                : HEADChatParticipantType.CLIENT;

        var otherPresence = chatPresenceService.getPresence(otherUuid, otherUserType);
        headChatActiveConversationStore.setActive(callerUuid,conversationId);
        return new HEADChatOpenConversationDto(
                conversationId,
                job.getId(),
                callerUuid,
                callerType,
                otherUuid,
                otherType,
                otherProfile,
                otherPresence,
                pageMessages.getNumber(),
                pageMessages.getSize(),
                pageMessages.getTotalElements(),
                pageMessages.getTotalPages(),
                items
        );
    }

    public void activeUser(String myUuid, String conversationId) {
        headChatActiveConversationStore.setActive(myUuid, conversationId);

        var otherUuid = otherUserUuid(conversationId, myUuid);

        chatEvents.chatActive(
                otherUuid,
                detectType(otherUuid),
                new HEADChatActiveDto(
                        myUuid,
                        detectType(myUuid),
                        conversationId,
                        true,
                        System.currentTimeMillis()
                )
        );
    }

    public void inactiveUserCaller(String myUuid, String conversationId) {
        headChatActiveConversationStore.clearActive(myUuid, conversationId);

        var otherUuid = otherUserUuid(conversationId, myUuid);

        chatEvents.chatInactive(
                otherUuid,
                detectType(otherUuid),
                new HEADChatActiveDto(
                        myUuid,
                        detectType(myUuid),
                        conversationId,
                        false,
                        System.currentTimeMillis()
                )
        );
    }


    public HEADConversationKey parseConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new HEADBadRequestException("conversationId is blank");
        }

        var parts = conversationId.split("\\|");
        if (parts.length != 3) {
            throw new HEADBadRequestException("Invalid conversationId format: " + conversationId);
        }

        Long jobId;
        try {
            jobId = Long.parseLong(parts[0]);
        } catch (Exception e) {
            throw new HEADBadRequestException("Invalid jobId in conversationId: " + conversationId);
        }

        return new HEADConversationKey(jobId, parts[1], parts[2]);
    }

    public String otherUserUuid(String conversationId, String myUuid) {
        var key = parseConversationId(conversationId);

        if (myUuid == null || myUuid.isBlank()) {
            throw new HEADBadRequestException("myUuid is blank");
        }

        if (myUuid.equals(key.firstUuid())) return key.secondUuid();
        if (myUuid.equals(key.secondUuid())) return key.firstUuid();

        throw new HEADBadRequestException("myUuid not part of conversationId");
    }

    public void renew(String uuIdUser) {
        headChatActiveConversationStore.renew(uuIdUser);
    }
}
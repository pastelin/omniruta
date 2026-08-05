package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service;

import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADAutenticationToken.HEADJwtGenerator;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADEntities.HEADPageResponse;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationText;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationsResponse;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADNotificationInbox;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationUiType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository.HEADNotificationInboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class HEADNotificationsApiService {

    private final HEADNotificationInboxRepository repo;
    private final HEADJwtGenerator jwt;

    public HEADNotificationsResponse list(int page, int size) {
        String uuid = jwt.getUserNamePersonalUser();

        var p = repo.findByUserUuidAndDeletedFalseOrderByCreatedAtDesc(uuid, PageRequest.of(page, size));

        long total = repo.countByUserUuidAndDeletedFalse(uuid);
        long unread = repo.countByUserUuidAndDeletedFalseAndIsReadFalse(uuid);

        var items = p.getContent().stream()
                .map(n -> new HEADNotificationsResponse.Item(
                        n.getId(),
                        n.getUiType().name(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getCreatedAt().toString(), // o formatTimeAgo(n.getCreatedAt())
                        n.isRead(),
                        n.getIcon()
                ))
                .toList();

        var pageResp = new HEADPageResponse<>(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.hasNext(),
                p.hasNext() ? p.getNumber() + 1 : null
        );

        return new HEADNotificationsResponse(
                new HEADNotificationsResponse.Summary(total, unread),
                pageResp
        );
    }

    public HEADNotificationsResponse.Summary summary() {
        String uuid = jwt.getUserNamePersonalUser();
        long total = repo.countByUserUuidAndDeletedFalse(uuid);
        long unread = repo.countByUserUuidAndDeletedFalseAndIsReadFalse(uuid);
        return new HEADNotificationsResponse.Summary(total, unread);
    }

    @Transactional
    public void markRead(long id) {
        String uuid = jwt.getUserNamePersonalUser();
        var n = repo.findByIdAndUserUuidAndDeletedFalse(id, uuid)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        if (!n.isRead()) {
            n.markRead();
            repo.save(n);
        }
    }

    @Transactional
    public void markAllRead() {
        String uuid = jwt.getUserNamePersonalUser();
        repo.markAllRead(uuid, Instant.now());
    }

    @Transactional
    public void delete(long id) {
        String uuid = jwt.getUserNamePersonalUser();
        var n = repo.findByIdAndUserUuidAndDeletedFalse(id, uuid)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        n.softDelete();
        repo.save(n);
    }
}
package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Service;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationCommand;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.dto.HEADNotificationText;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADNotificationInbox;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Enums.HEADNotificationUiType;
import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository.HEADNotificationInboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HEADNotificationInboxService {

    private final HEADNotificationInboxRepository repo;
    private final ObjectMapper om;

    private static final boolean ENABLE_PROMOTIONS = true;

    @Transactional
    public void upsertRelevant(HEADNotificationCommand cmd, HEADNotificationText text) {
        if (!shouldStore(cmd)) return;

        String userUuid = cmd.userUuid();
        String dedupeKey = buildDedupeKey(cmd);

        repo.findFirstByUserUuidAndDedupeKeyAndDeletedFalseOrderByCreatedAtDesc(userUuid, dedupeKey)
                .ifPresentOrElse(existing -> {
                    existing.setTitle(safeTitle(text.title()));
                    existing.setMessage(safeBody(text.body()));
                    existing.setIcon(resolveIcon(cmd));
                    existing.setDataJson(writeJson(cmd.params()));
                    existing.setRead(false);
                    existing.setReadAt(null);
                    // createdAt no cambia (si quieres que “suba”, puedes setCreatedAt(Instant.now()) pero requiere permitir update)
                    repo.save(existing);
                }, () -> repo.save(buildNew(cmd, text, dedupeKey)));

    }

    private HEADNotificationInbox buildNew(HEADNotificationCommand cmd, HEADNotificationText text, String dedupeKey) {
        var n = new HEADNotificationInbox();
        n.setUserUuid(cmd.userUuid());
        n.setEventType(cmd.type());
        n.setUiType(mapUiType(cmd.type()));
        n.setTitle(safeTitle(text.title()));
        n.setMessage(safeBody(text.body()));
        n.setIcon(resolveIcon(cmd));
        n.setDedupeKey(dedupeKey);
        n.setDataJson(writeJson(cmd.params()));
        return n;
    }

    private boolean shouldStore(HEADNotificationCommand cmd) {
        return switch (cmd.type()) {
            case APPOINTMENT_REMINDER -> true;
            case MED_REMINDER -> true;
            case PRESCRIPTION_ISSUED -> true;
            case PROMOTION -> ENABLE_PROMOTIONS;
            default -> false;
        };
    }

    private HEADNotificationUiType mapUiType(HEADNotificationType t) {
        return switch (t) {
            case SCHEDULE, APPOINTMENT_REMINDER -> HEADNotificationUiType.APPOINTMENT;
            case MED_REMINDER -> HEADNotificationUiType.REMINDER;
            case PRESCRIPTION_ISSUED -> HEADNotificationUiType.INFO;
            case PROMOTION -> HEADNotificationUiType.PROMOTION;
            default -> HEADNotificationUiType.INFO;
        };
    }

    private String buildDedupeKey(HEADNotificationCommand cmd) {

        if (cmd.type() == HEADNotificationType.APPOINTMENT_REMINDER) {
            Object jobId = cmd.params().get("jobId");
            return jobId == null ? null : "APT_REMINDER_" + jobId;
        }

        if (cmd.type() == HEADNotificationType.MED_REMINDER) {
            Object doseId = cmd.params().get("doseId");
            return doseId == null ? null : "MED_DOSE_" + doseId;
        }

        if (cmd.type() == HEADNotificationType.PRESCRIPTION_ISSUED) {
            Object prescriptionId = cmd.params().get("prescriptionId");
            if (prescriptionId != null) return "RX_" + prescriptionId;
            Object jobId = cmd.params().get("jobId");
            return jobId == null ? null : "RX_JOB_" + jobId;
        }

        if (cmd.type() == HEADNotificationType.PROMOTION) {
            Object promoId = cmd.params().get("promoId");
            return promoId == null ? null : "PROMO_" + promoId;
        }

        return null;
    }

    private String resolveIcon(HEADNotificationCommand cmd) {
        Object icon = cmd.params().get("icon");
        if (icon != null) return String.valueOf(icon);

        return switch (cmd.type()) {
            case SCHEDULE -> "📅";
            case MED_REMINDER -> "💊";
            case PRESCRIPTION_ISSUED -> "🧾";
            case PROMOTION -> "🏷️";
            default -> "🔔";
        };
    }

    private String writeJson(Object o) {
        try { return om.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }

    private String safeTitle(String t) { return (t == null || t.isBlank()) ? "Notificación" : t; }
    private String safeBody(String b) { return (b == null) ? "" : (b.length() > 700 ? b.substring(0,700) : b); }
}
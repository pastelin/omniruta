package com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.repository;

import com.HealthAdvanced.healthAdvanced.HEADNotificationsPush.Entity.model.HEADNotificationInbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface HEADNotificationInboxRepository extends JpaRepository<HEADNotificationInbox, Long> {

    Page<HEADNotificationInbox> findByUserUuidAndDeletedFalseOrderByCreatedAtDesc(
            String userUuid, Pageable pageable);

    long countByUserUuidAndDeletedFalse(String userUuid);
    long countByUserUuidAndDeletedFalseAndIsReadFalse(String userUuid);

    Optional<HEADNotificationInbox> findByIdAndUserUuidAndDeletedFalse(Long id, String userUuid);

    Optional<HEADNotificationInbox> findFirstByUserUuidAndDedupeKeyAndDeletedFalseOrderByCreatedAtDesc(
            String userUuid, String dedupeKey
    );

    @Modifying
    @Query("""
        update HEADNotificationInbox n
           set n.isRead = true, n.readAt = :now
         where n.userUuid = :userUuid
           and n.deleted = false
           and n.isRead = false
    """)
    int markAllRead(@Param("userUuid") String userUuid, @Param("now") Instant now);
}
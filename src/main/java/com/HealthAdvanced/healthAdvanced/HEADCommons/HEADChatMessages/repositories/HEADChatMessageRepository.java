package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.repositories;


import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.entity.model.HEADChatMessage;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.enums.HEADChatMessageStatus;
import com.HealthAdvanced.healthAdvanced.HEADCommons.HEADChatMessages.interfaces.HEADChatUnreadSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HEADChatMessageRepository extends JpaRepository<HEADChatMessage, Long> {

    Page<HEADChatMessage> findByConversationIdOrderByCreatedAtAsc(
            String conversationId,
            Pageable pageable
    );

    List<HEADChatMessage> findByConversationIdAndRecipientUuidAndStatusNot(
            String conversationId,
            String recipientUuid,
            HEADChatMessageStatus status
    );

    @Query("""
        select count(m)
        from HEADChatMessage m
        where m.recipientUuid = :recipientUuid
          and m.status <> :readStatus
        """)
    long countUnreadMessages(
            @Param("recipientUuid") String recipientUuid,
            @Param("readStatus") HEADChatMessageStatus readStatus
    );

    @Query("""
        select m.conversationId as conversationId,
               count(m)          as unreadCount
        from HEADChatMessage m
        where m.recipientUuid = :recipientUuid
          and m.status <> :readStatus
        group by m.conversationId
        """)
    List<HEADChatUnreadSummary> findUnreadByConversation(
            @Param("recipientUuid") String recipientUuid,
            @Param("readStatus") HEADChatMessageStatus readStatus
    );
}
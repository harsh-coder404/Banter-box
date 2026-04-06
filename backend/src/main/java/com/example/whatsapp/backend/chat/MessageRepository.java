package com.example.whatsapp.backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            Long senderA,
            Long receiverA,
            Long senderB,
            Long receiverB
    );

    @Query("""
            select m from MessageEntity m
            where m.id = :messageId
              and (m.senderId = :userId or m.receiverId = :userId)
            """)
    Optional<MessageEntity> findParticipantMessage(
            @Param("messageId") Long messageId,
            @Param("userId") Long userId
    );
}

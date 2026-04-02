package com.example.whatsapp.backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    List<MessageEntity> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            Long senderA,
            Long receiverA,
            Long senderB,
            Long receiverB
    );
}


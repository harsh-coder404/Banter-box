package com.example.whatsapp.backend.chat;

import com.example.whatsapp.backend.chat.dto.MessageDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageDto save(Long senderId, Long receiverId, String content) {
        MessageEntity entity = new MessageEntity();
        entity.setSenderId(senderId);
        entity.setReceiverId(receiverId);
        entity.setContent(content);
        entity.setCreatedAt(Instant.now());

        MessageEntity saved = messageRepository.save(entity);
        return toDto(saved);
    }

    public List<MessageDto> history(Long userId, Long otherUserId) {
        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                        userId,
                        otherUserId,
                        otherUserId,
                        userId
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    public String chatKey(Long a, Long b) {
        long min = Math.min(a, b);
        long max = Math.max(a, b);
        return min + "_" + max;
    }

    private MessageDto toDto(MessageEntity entity) {
        return new MessageDto(
                entity.getId(),
                entity.getSenderId(),
                entity.getReceiverId(),
                entity.getContent(),
                entity.getCreatedAt()
        );
    }
}


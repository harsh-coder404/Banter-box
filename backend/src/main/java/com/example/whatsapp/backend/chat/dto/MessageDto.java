package com.example.whatsapp.backend.chat.dto;

import java.time.Instant;

public record MessageDto(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        Instant createdAt
) {
}


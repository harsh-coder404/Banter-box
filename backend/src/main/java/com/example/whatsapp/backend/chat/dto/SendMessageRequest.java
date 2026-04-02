package com.example.whatsapp.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendMessageRequest(
        @NotNull Long senderId,
        @NotNull Long receiverId,
        @NotBlank String content
) {
}


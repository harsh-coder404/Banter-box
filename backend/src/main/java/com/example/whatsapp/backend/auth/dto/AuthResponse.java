package com.example.whatsapp.backend.auth.dto;

public record AuthResponse(
        Long userId,
        String name,
        String phoneNumber,
        String token
) {
}


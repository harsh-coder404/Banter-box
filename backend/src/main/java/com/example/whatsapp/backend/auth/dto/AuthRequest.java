package com.example.whatsapp.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String phoneNumber,
        @NotBlank String password
) {
}


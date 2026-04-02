package com.example.whatsapp.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank String phoneNumber,
        @NotBlank String password
) {
}


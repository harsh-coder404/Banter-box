package com.example.whatsapp.backend.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AddContactRequest(@NotBlank String contactPhoneNumber) {
}


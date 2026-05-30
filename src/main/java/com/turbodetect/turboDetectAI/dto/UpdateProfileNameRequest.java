package com.turbodetect.turboDetectAI.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileNameRequest(
        @NotBlank(message = "Full name is required")
        String fullName
) {
}
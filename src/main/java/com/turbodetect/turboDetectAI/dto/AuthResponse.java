package com.turbodetect.turboDetectAI.dto;

public record AuthResponse(
        String token,
        String fullName,
        String email,
        String profileImageUrl
) {
}
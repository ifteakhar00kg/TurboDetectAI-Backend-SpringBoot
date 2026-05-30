package com.turbodetect.turboDetectAI.dto;

public record ProfileResponse(
        Long id,
        String fullName,
        String email,
        String profileImageUrl
) {
}
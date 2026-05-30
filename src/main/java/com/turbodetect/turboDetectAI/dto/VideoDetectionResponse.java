package com.turbodetect.turboDetectAI.dto;

import java.time.LocalDateTime;

public record VideoDetectionResponse(
        Long id,
        String fileName,
        String scanType,
        String result,
        int fakePercentage,
        String confidence,
        LocalDateTime scannedAt
) {
}
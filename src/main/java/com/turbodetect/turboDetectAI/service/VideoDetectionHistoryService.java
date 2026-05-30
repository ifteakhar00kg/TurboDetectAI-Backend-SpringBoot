package com.turbodetect.turboDetectAI.service;

import com.turbodetect.turboDetectAI.entity.VideoDetectionHistory;
import com.turbodetect.turboDetectAI.repository.VideoDetectionHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoDetectionHistoryService {

    private final VideoDetectionHistoryRepository historyRepository;

    public VideoDetectionHistory saveHistory(
            String userEmail,
            String fileName,
            String scanType,
            String result,
            int fakePercentage,
            String confidence
    ) {
        VideoDetectionHistory history = VideoDetectionHistory.builder()
                .userEmail(userEmail)
                .fileName(fileName)
                .scanType(scanType)
                .result(result)
                .fakePercentage(fakePercentage)
                .confidence(confidence)
                .scannedAt(LocalDateTime.now())
                .build();

        return historyRepository.save(history);
    }

    public List<VideoDetectionHistory> getUserHistory(String userEmail) {
        return historyRepository.findByUserEmailOrderByScannedAtDesc(userEmail);
    }

    public void deleteHistory(Long id, String userEmail) {
        VideoDetectionHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History not found"));

        if (!history.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You cannot delete this history");
        }

        historyRepository.delete(history);
    }

    public long getUserVideoCount(String userEmail) {
        return historyRepository.countByUserEmail(userEmail);
    }
}
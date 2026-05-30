package com.turbodetect.turboDetectAI.repository;

import com.turbodetect.turboDetectAI.entity.VideoDetectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoDetectionHistoryRepository extends JpaRepository<VideoDetectionHistory, Long> {

    List<VideoDetectionHistory> findByUserEmailOrderByScannedAtDesc(String userEmail);

    long countByUserEmail(String userEmail);
}
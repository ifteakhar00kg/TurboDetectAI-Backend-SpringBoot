package com.turbodetect.turboDetectAI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoDetectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;

    private String fileName;

    private String scanType;

    private String result;

    private int fakePercentage;

    private String confidence;

    private LocalDateTime scannedAt;
}
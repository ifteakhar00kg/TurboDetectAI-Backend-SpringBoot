package com.turbodetect.turboDetectAI.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String profileImageUrl;

    private Boolean enabled;

    private String verificationCode;

    private LocalDateTime verificationCodeExpiresAt;

    private LocalDateTime otpLastSentAt;

    private Integer otpRequestCount;

    private LocalDate otpRequestDate;
}
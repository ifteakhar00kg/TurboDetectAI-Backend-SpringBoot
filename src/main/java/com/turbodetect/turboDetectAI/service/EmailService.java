package com.turbodetect.turboDetectAI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("TurboDetect AI - Verification Code");

        message.setText(
                "Hello,\n\n" +
                        "Your TurboDetect AI verification code is: " + code + "\n\n" +
                        "This code will expire in 10 minutes. If you did not request this verification, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Team TurboDetect AI"
        );

        mailSender.send(message);
    }
}
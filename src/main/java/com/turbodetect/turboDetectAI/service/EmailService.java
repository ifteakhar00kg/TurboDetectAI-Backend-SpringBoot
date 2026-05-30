package com.turbodetect.turboDetectAI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);
        message.setSubject("TurboDetect AI Email Verification");

        message.setText(
                "━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "        TurboDetect AI\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━\n\n" +

                        "Hello,\n\n" +

                        "Welcome to TurboDetect AI 🔐\n" +
                        "Your account verification code is:\n\n" +

                        "        " + code + "\n\n" +

                        "⏳ This code will expire in 10 minutes.\n\n" +

                        "You can request a new code after 1 minute.\n" +
                        "Maximum 10 OTP requests are allowed per day.\n\n" +

                        "If you did not request this verification,\n" +
                        "please ignore this email.\n\n" +

                        "Stay secure,\n" +
                        "Team TurboDetect AI 🚀\n\n" +

                        "━━━━━━━━━━━━━━━━━━━━━━"
        );

        mailSender.send(message);
    }
}
package com.turbodetect.turboDetectAI.service;

import com.turbodetect.turboDetectAI.dto.AuthResponse;
import com.turbodetect.turboDetectAI.dto.ForgotPasswordRequest;
import com.turbodetect.turboDetectAI.dto.LoginRequest;
import com.turbodetect.turboDetectAI.dto.RegisterRequest;
import com.turbodetect.turboDetectAI.dto.ResetPasswordRequest;
import com.turbodetect.turboDetectAI.dto.VerifyEmailRequest;
import com.turbodetect.turboDetectAI.dto.VerifyResetCodeRequest;
import com.turbodetect.turboDetectAI.entity.User;
import com.turbodetect.turboDetectAI.repository.UserRepository;
import com.turbodetect.turboDetectAI.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public String register(RegisterRequest request) {

        User existingUser = userRepository.findByEmail(request.email())
                .orElse(null);

        if (existingUser != null) {

            if (Boolean.TRUE.equals(existingUser.getEnabled())) {
                throw new IllegalArgumentException("Email already registered");
            }

            checkOtpLimit(existingUser);

            String newCode = generateVerificationCode();

            existingUser.setFullName(request.fullName());
            existingUser.setPassword(passwordEncoder.encode(request.password()));
            existingUser.setVerificationCode(newCode);
            existingUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
            existingUser.setOtpLastSentAt(LocalDateTime.now());
            existingUser.setOtpRequestDate(LocalDate.now());
            existingUser.setOtpRequestCount(existingUser.getOtpRequestCount() + 1);

            userRepository.save(existingUser);

            emailService.sendVerificationCode(existingUser.getEmail(), newCode);

            return "New verification code sent to your email";
        }

        String code = generateVerificationCode();

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .profileImageUrl(null)
                .enabled(false)
                .verificationCode(code)
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10))
                .otpLastSentAt(LocalDateTime.now())
                .otpRequestDate(LocalDate.now())
                .otpRequestCount(1)
                .build();

        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), code);

        return "Verification code sent to your email";
    }

    public String verifyEmail(VerifyEmailRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (Boolean.TRUE.equals(user.getEnabled())) {
            return "Account already verified";
        }

        if (user.getVerificationCode() == null) {
            throw new IllegalArgumentException("Verification code not found");
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!user.getVerificationCode().equals(request.code())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);

        return "Registration completed successfully";
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalArgumentException("Please verify your email first");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getFullName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }

    public String forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new IllegalArgumentException("Please verify your email first");
        }

        checkOtpLimit(user);

        String code = generateVerificationCode();

        user.setVerificationCode(code);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        user.setOtpLastSentAt(LocalDateTime.now());
        user.setOtpRequestDate(LocalDate.now());
        user.setOtpRequestCount(user.getOtpRequestCount() + 1);

        userRepository.save(user);

        emailService.sendVerificationCode(user.getEmail(), code);

        return "Password reset code sent to your email";
    }

    public String verifyResetCode(VerifyResetCodeRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getVerificationCode() == null) {
            throw new IllegalArgumentException("Verification code not found");
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!user.getVerificationCode().equals(request.code())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        return "Code verified successfully";
    }

    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getVerificationCode() == null) {
            throw new IllegalArgumentException("Verification code not found");
        }

        if (user.getVerificationCodeExpiresAt() == null ||
                user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired");
        }

        if (!user.getVerificationCode().equals(request.code())) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);

        return "Password reset successfully";
    }

    private void checkOtpLimit(User user) {

        LocalDate today = LocalDate.now();

        if (user.getOtpRequestDate() == null ||
                !user.getOtpRequestDate().equals(today)) {

            user.setOtpRequestDate(today);
            user.setOtpRequestCount(0);
            user.setOtpLastSentAt(null);
        }

        if (user.getOtpRequestCount() != null &&
                user.getOtpRequestCount() >= 10) {
            throw new IllegalArgumentException("Daily OTP limit reached. Please try again tomorrow.");
        }

        if (user.getOtpLastSentAt() != null) {
            long seconds = Duration.between(
                    user.getOtpLastSentAt(),
                    LocalDateTime.now()
            ).getSeconds();

            if (seconds < 60) {
                throw new IllegalArgumentException("Please wait 1 minute before requesting another OTP.");
            }
        }

        if (user.getOtpRequestCount() == null) {
            user.setOtpRequestCount(0);
        }
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}
package com.turbodetect.turboDetectAI.controller;

import com.turbodetect.turboDetectAI.dto.AuthResponse;
import com.turbodetect.turboDetectAI.dto.ForgotPasswordRequest;
import com.turbodetect.turboDetectAI.dto.LoginRequest;
import com.turbodetect.turboDetectAI.dto.RegisterRequest;
import com.turbodetect.turboDetectAI.dto.ResetPasswordRequest;
import com.turbodetect.turboDetectAI.dto.VerifyEmailRequest;
import com.turbodetect.turboDetectAI.dto.VerifyResetCodeRequest;
import com.turbodetect.turboDetectAI.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/verify-reset-code")
    public String verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        return authService.verifyResetCode(request);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
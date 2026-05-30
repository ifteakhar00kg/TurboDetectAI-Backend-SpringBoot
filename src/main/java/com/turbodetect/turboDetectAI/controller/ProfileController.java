package com.turbodetect.turboDetectAI.controller;

import com.turbodetect.turboDetectAI.dto.ProfileResponse;
import com.turbodetect.turboDetectAI.dto.UpdateProfileNameRequest;
import com.turbodetect.turboDetectAI.entity.User;
import com.turbodetect.turboDetectAI.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public ProfileResponse getProfile(HttpServletRequest request) {
        String email = (String) request.getAttribute("userEmail");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toProfileResponse(user);
    }

    @PutMapping("/name")
    public ProfileResponse updateName(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileNameRequest updateRequest
    ) {
        String email = (String) request.getAttribute("userEmail");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFullName(updateRequest.fullName());
        userRepository.save(user);

        return toProfileResponse(user);
    }

    @PostMapping("/image")
    public ProfileResponse uploadProfileImage(
            HttpServletRequest request,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        String email = (String) request.getAttribute("userEmail");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String uploadDir = "uploads/profile-images/";
        File directory = new File(uploadDir);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);

        Files.write(filePath, image.getBytes());

        String imageUrl = "/uploads/profile-images/" + fileName;

        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return toProfileResponse(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getProfileImageUrl()
        );
    }
}
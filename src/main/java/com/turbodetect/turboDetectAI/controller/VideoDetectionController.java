package com.turbodetect.turboDetectAI.controller;

import com.turbodetect.turboDetectAI.entity.VideoDetectionHistory;
import com.turbodetect.turboDetectAI.service.VideoDetectionHistoryService;
import com.turbodetect.turboDetectAI.service.DeepfakeService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class VideoDetectionController {

    private final VideoDetectionHistoryService historyService;
    private final DeepfakeService deepfakeService; // Python-এর সাথে কানেক্ট করার সার্ভিস

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping({
            "/api/video/detect",
            "/api/video-detection/scan"
    })
    public Map<String, Object> detectVideo(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "video", required = false) MultipartFile video,
            @RequestParam(value = "scanType", required = false) String scanType,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        MultipartFile uploadedFile = file != null ? file : video;

        if (uploadedFile == null || uploadedFile.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "No video file received"
            );
        }

        String userEmail = getUserEmail(authHeader, authentication);

        if (scanType == null || scanType.isBlank()) {
            scanType = "DEEP";
        }
        scanType = scanType.toUpperCase();

        // ডিফল্ট ভ্যালু (যদি কোনো কারণে পাইথন সার্ভার ফেইল করে)
        String result = "UNKNOWN";
        int fakePercentage = 0;
        String confidence = "HIGH";
        String detailedAnalysis = null;

        try {
            // Python API (FastAPI) কে লাইভ কল করা হচ্ছে
            ResponseEntity<String> pythonResponse = deepfakeService.analyzeFile(uploadedFile, scanType);

            if (pythonResponse.getStatusCode().is2xxSuccessful() && pythonResponse.getBody() != null) {
                // Spring Boot এর নিজস্ব পার্সার দিয়ে JSON রিড করা হচ্ছে
                JsonParser springParser = JsonParserFactory.getJsonParser();
                Map<String, Object> jsonMap = springParser.parseMap(pythonResponse.getBody());

                result = (String) jsonMap.get("verdict");
                fakePercentage = (int) Math.round(((Number) jsonMap.get("fake_score")).doubleValue());

                if (jsonMap.containsKey("detailed_analysis")) {
                    detailedAnalysis = (String) jsonMap.get("detailed_analysis");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "AI Analysis failed: " + e.getMessage());
        }

        // আসল AI রেজাল্ট ডেটাবেসে সেভ করা হচ্ছে
        VideoDetectionHistory saved = historyService.saveHistory(
                userEmail,
                uploadedFile.getOriginalFilename(),
                scanType,
                result,
                fakePercentage,
                confidence
        );

        // ফ্রন্টএন্ডে রেজাল্ট পাঠানো হচ্ছে
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("id", saved.getId());
        response.put("fileName", saved.getFileName());
        response.put("scanType", saved.getScanType());
        response.put("result", saved.getResult());
        response.put("fakePercentage", saved.getFakePercentage());
        response.put("confidence", saved.getConfidence());
        response.put("scannedAt", saved.getScannedAt());
        response.put("message", "Video scanned successfully");

        if (detailedAnalysis != null) {
            response.put("detailed_analysis", detailedAnalysis);
        }

        return response;
    }

    @GetMapping({
            "/api/video/history",
            "/api/video-detection/history"
    })
    public List<VideoDetectionHistory> getMyHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        String userEmail = getUserEmail(authHeader, authentication);
        return historyService.getUserHistory(userEmail);
    }

    @DeleteMapping({
            "/api/video/history/{id}",
            "/api/video-detection/history/{id}"
    })
    public Map<String, String> deleteHistory(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        String userEmail = getUserEmail(authHeader, authentication);
        historyService.deleteHistory(id, userEmail);

        return Map.of("message", "History deleted successfully");
    }

    @GetMapping("/api/video-detection/stats")
    public Map<String, Object> getStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication authentication
    ) {
        String userEmail = getUserEmail(authHeader, authentication);

        long videoCount = historyService.getUserVideoCount(userEmail);

        return Map.of(
                "videos", videoCount,
                "voice", 0,
                "faces", 0
        );
    }

    private String getUserEmail(String authHeader, Authentication authentication) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                return claims.getSubject();
            } catch (Exception e) {
                System.out.println("JWT decode failed: " + e.getMessage());
            }
        }

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }

        return "guest";
    }
}
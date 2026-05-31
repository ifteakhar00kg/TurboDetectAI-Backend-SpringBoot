package com.turbodetect.turboDetectAI.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DeepfakeService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_API_URL = "https://ifteakar-turbo-detect-ai.hf.space";

    public ResponseEntity<String> analyzeFile(MultipartFile file, String scanType) throws Exception {
        String endpoint = scanType.equalsIgnoreCase("deep") ? "/deep-scan" : "/quick-scan";
        String url = PYTHON_API_URL + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, requestEntity, String.class);
    }
}
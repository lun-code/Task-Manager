package com.example.task_manager_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    @Value("${BREVO_API_KEY}")
    private String apiKey;

    public void sendVerificationEmail(String to, String token) {

        String verificationUrl = "https://task-manager-nto7.vercel.app/verify?token=" + token;

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        body.put("sender", Map.of(
                "email", "lun.code01@gmail.com",
                "name", "Task Manager"
        ));

        body.put("to", List.of(
                Map.of("email", to)
        ));

        body.put("subject", "Verifica tu cuenta");

        body.put("htmlContent",
                "<p>Haz clic en el siguiente enlace para verificar tu cuenta:</p>" +
                        "<a href=\"" + verificationUrl + "\">Verificar cuenta</a>"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(BREVO_API_URL, request, String.class);
    }
}
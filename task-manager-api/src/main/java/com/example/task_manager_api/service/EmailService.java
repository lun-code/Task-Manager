package com.example.task_manager_api.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    public void sendVerificationEmail(String userEmail, String token) {
        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(userEmail)
                .subject("Verifica tu cuenta")
                .html("<p>Haz clic en el siguiente enlace para verificar tu cuenta:</p>" +
                        "<a href='https://task-manager-nto7.vercel.app/verify?token=" + token + "'>Verificar cuenta</a>")
                .build();

        try {
            resend.emails().send(params);
        } catch (ResendException e) {
            throw new RuntimeException("Error al enviar el email", e);
        }
    }
}
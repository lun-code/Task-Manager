package com.example.task_manager_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verifica tu cuenta");
        message.setText("Haz clic en el siguiente enlace para verificar tu cuenta:\n\n"
                + "http://localhost:8080/api/auth/verify?token=" + token);

        mailSender.send(message);
    }
}
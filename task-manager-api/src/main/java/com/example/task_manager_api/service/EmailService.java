package com.example.task_manager_api.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

public class EmailService {
    public void sendVerificationEmail(String userEmail, String token) throws ResendException {
        Resend resend = new Resend("re_LizEknLZ_8wivDtqWQtW9RwXLvzgVuzNQ");

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(userEmail)
                .subject("Verificación")
                .html("<p>Haz click en el enlace para confirmar tu cuenta: \n\n</p>" + "<p>https://task-manager-nto7.vercel.app/verify?token=</p>" + token)
                .build();

        CreateEmailResponse data = resend.emails().send(params);
    }
}
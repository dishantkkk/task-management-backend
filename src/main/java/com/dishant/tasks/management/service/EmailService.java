package com.dishant.tasks.management.service;

import com.dishant.tasks.management.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    public void sendVerificationEmail(User user) {
        String verificationLink = "http://localhost:8080/v1/api/auth/verify?token=" + user.getVerificationToken();
        String subject = "Verify your email";
        String body = "Hi " + user.getName() + ",\n\nPlease verify your email by clicking the link below:\n" + verificationLink;
        log.info("📧 Send this verification link to user: {}", verificationLink);
    }
}

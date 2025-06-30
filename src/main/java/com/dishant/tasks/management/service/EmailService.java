package com.dishant.tasks.management.service;

import com.dishant.tasks.management.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private JavaMailSender javaMailSender;

    public void sendVerificationEmail(User user, String resetLink) {
        String verificationLink = "http://localhost:8080/v1/api/auth/verify?token=" + user.getVerificationToken();

        log.info("Preparing to send verification email to: {} and link: {}", user.getEmail(), resetLink);
        log.debug("Verification link generated for user {}: {} and reset link: {}", user.getUsername(), verificationLink, resetLink);
        log.info("📧 Send this verification link to user: {}", verificationLink);
    }

    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {} | Subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

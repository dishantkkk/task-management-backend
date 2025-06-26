package com.dishant.tasks.management.service;

import com.dishant.tasks.management.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmail_shouldSendSimpleMailMessage() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "This is the test body";

        emailService.sendEmail(to, subject, body);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assert sentMessage.getTo() != null;
        assert sentMessage.getTo()[0].equals(to);
        assert sentMessage.getSubject().equals(subject);
        assert sentMessage.getText().equals(body);
    }

    @Test
    void testSendVerificationEmail_shouldLogAndNotSendEmailDirectly() {
        User user = new User();
        user.setName("John");
        user.setEmail("john@example.com");
        user.setVerificationToken("abc123");

        emailService.sendVerificationEmail(user);

        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}

package com.dishant.tasks.management.service;

import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setEnabled(false);
        user.setPassword("encodedpass");
        user.setRole(Role.USER);
    }

    @Test
    void registerUser_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("name", "Test");
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "pass123");
        request.put("confirmPassword", "pass123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass123")).thenReturn("encodedpass");

        Optional<User> result = userService.registerUser(request);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(User.class));
    }

    @Test
    void registerUser_PasswordMismatch() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "pass123");
        request.put("confirmPassword", "pass456");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Passwords do not match", ex.getMessage());
    }

    @Test
    void registerUser_UsernameExists() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "pass123");
        request.put("confirmPassword", "pass123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Username already exists", ex.getMessage());
    }

    @Test
    void registerUser_EmailExists() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "pass123");
        request.put("confirmPassword", "pass123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(request));
        assertEquals("Email already registered", ex.getMessage());
    }

    @Test
    void verifyEmail_Success() {
        user.setVerificationToken("abc123");

        when(userRepository.findByVerificationToken("abc123")).thenReturn(Optional.of(user));

        boolean result = userService.verifyEmail("abc123");

        assertTrue(result);
        assertTrue(user.isEnabled());
        assertNull(user.getVerificationToken());
        verify(userRepository).save(user);
    }

    @Test
    void verifyEmail_InvalidToken() {
        when(userRepository.findByVerificationToken("invalid")).thenReturn(Optional.empty());

        boolean result = userService.verifyEmail("invalid");
        assertFalse(result);
    }

    @Test
    void resendVerification_Success() {
        user.setEnabled(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.resendVerification("test@example.com");

        assertNotNull(user.getVerificationToken());
        verify(userRepository).save(user);
        verify(emailService).sendVerificationEmail(user);
    }

    @Test
    void resendVerification_EmailNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resendVerification("test@example.com"));

        assertEquals("Email not found", ex.getMessage());
    }

    @Test
    void resendVerification_EmailAlreadyVerified() {
        user.setEnabled(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resendVerification("test@example.com"));

        assertEquals("Email already verified", ex.getMessage());
    }

    @Test
    void initiatePasswordReset_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        userService.initiatePasswordReset("test@example.com");

        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());
        verify(userRepository).save(user);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), contains("reset-password"));
    }

    @Test
    void initiatePasswordReset_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.initiatePasswordReset("notfound@example.com"));

        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void resetPassword_Success() {
        user.setResetToken("reset123");
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByResetToken("reset123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");

        userService.resetPassword("reset123", "newpass");

        assertEquals("encodedNewPass", user.getPassword());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_InvalidToken() {
        when(userRepository.findByResetToken("invalid")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("invalid", "pass"));

        assertEquals("Invalid token", ex.getMessage());
    }

    @Test
    void resetPassword_ExpiredToken() {
        user.setResetToken("expired123");
        user.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByResetToken("expired123")).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resetPassword("expired123", "pass"));

        assertEquals("Token expired", ex.getMessage());
    }
}

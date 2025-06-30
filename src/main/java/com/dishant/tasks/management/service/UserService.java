package com.dishant.tasks.management.service;

import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.dishant.tasks.management.constants.Constants.EMAIL;
import static com.dishant.tasks.management.constants.Constants.USER_NOT_FOUND_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public Optional<User> registerUser(Map<String, String> requestData) {
        String name = requestData.get("name");
        String username = requestData.get("username");
        String email = requestData.get(EMAIL);
        String password = requestData.get("password");
        String confirmPassword = requestData.get("confirmPassword");

        log.debug("Attempting to register user: {}", username);

        if (!password.equals(confirmPassword)) {
            log.warn("Password and confirm password do not match for user: {}", username);
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Email already registered: {}", email);
            throw new IllegalArgumentException("Email already registered");
        }

        String token = UUID.randomUUID().toString();

        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setEnabled(false);
        user.setVerificationToken(token);

        userRepository.save(user);
        log.debug("User saved to repository: {}", username);

        emailService.sendVerificationEmail(user, "Abc");
        log.debug("Verification email sent to: {}", email);

        return Optional.of(user);
    }

    public boolean verifyEmail(String token) {
        log.debug("Verifying email with token: {}", token);
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            log.warn("Invalid or expired verification token: {}", token);
            return false;
        }

        User user = userOpt.get();
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.debug("Email verification successful for user: {}", user.getUsername());
        return true;
    }

    public void resendVerification(String email) {
        log.debug("Resending verification email to: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Email not found for resend verification: {}", email);
            throw new IllegalArgumentException("Email not found");
        }

        User user = userOpt.get();
        if (user.isEnabled()) {
            log.warn("Attempted to resend verification to already verified email: {}", email);
            throw new IllegalArgumentException("Email already verified");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user, "abc");
        log.debug("New verification email sent to: {}", email);
    }

    public void initiatePasswordReset(String email) {
        log.debug("Initiating password reset for email: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("User not found for password reset: {}", email);
            throw new IllegalArgumentException(USER_NOT_FOUND_ERROR_MESSAGE);
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        String resetLink = "http://localhost:5173/reset-password/" + token;
        log.debug("Password reset token generated for user: {}, token: {}", user.getUsername(), token);

        emailService.sendVerificationEmail(user, resetLink); // or use sendEmail() with reset link if preferred
        log.debug("Password reset email sent to: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        log.debug("Resetting password using token: {}", token);
        Optional<User> userOpt = userRepository.findByResetToken(token);

        if (userOpt.isEmpty()) {
            log.warn("Invalid password reset token: {}", token);
            throw new IllegalArgumentException("Invalid token");
        }

        User user = userOpt.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            log.warn("Expired password reset token for user: {}", user.getUsername());
            throw new IllegalArgumentException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        log.debug("Password successfully reset for user: {}", user.getUsername());
    }
}

package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AuthRequest;
import com.dishant.tasks.management.dto.AuthResponse;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.security.CustomUserDetails;
import com.dishant.tasks.management.security.JwtService;
import com.dishant.tasks.management.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import static com.dishant.tasks.management.constants.Constants.EMAIL;
import static com.dishant.tasks.management.constants.Constants.USER_NOT_FOUND_ERROR_MESSAGE;

@RestController
@RequestMapping("/v1/api/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> request) {
        log.info("Received request to register user with username: {}", request.get("username"));
        try {
            Optional<User> userOptional = userService.registerUser(request);
            User user = userOptional.orElseThrow(() -> new IllegalArgumentException("User registration failed"));

            String jwt = jwtService.generateToken(new CustomUserDetails(user));
            log.debug("JWT token generated for user: {}", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    jwt,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            ));
        } catch (IllegalArgumentException ex) {
            log.error("Registration failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verifyEmail(@RequestParam("token") String token) {
        log.info("Received request to verify email with token");
        if (userService.verifyEmail(token)) {
            log.debug("Email verified successfully for token: {}", token);
            return ResponseEntity.ok("Email verified successfully. You can now log in.");
        }
        log.warn("Invalid or expired verification token: {}", token);
        return ResponseEntity.badRequest().body("Invalid or expired verification token");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Object> resendVerification(@RequestBody Map<String, String> payload) {
        log.info("Received request to resend verification email for: {}", payload.get(EMAIL));
        try {
            userService.resendVerification(payload.get(EMAIL));
            log.debug("Verification email resent to: {}", payload.get(EMAIL));
            return ResponseEntity.ok("Verification email resent");
        } catch (IllegalArgumentException ex) {
            log.error("Resend verification failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody AuthRequest request) {
        String identifier = request.getUsernameOrEmail();
        log.info("Received login request for: {}", identifier);

        try {
            Optional<User> userOpt = userRepository.findByEmail(identifier)
                    .or(() -> userRepository.findByUsername(identifier));

            if (userOpt.isEmpty()) {
                log.warn("Login failed. User not found: {}", identifier);
                return ResponseEntity.status(401).body("Invalid username/email or password");
            }

            User u = userOpt.get();
            if (!u.isEnabled()) {
                log.warn("Login attempt with unverified email: {}", identifier);
                return ResponseEntity.status(401).body("Email not verified");
            }

            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword()));
            log.debug("Authentication successful for user: {}", identifier);

            CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
            User user = customUserDetails.getUser();
            String token = jwtService.generateToken(customUserDetails);
            log.debug("JWT token generated for user: {}", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    token, user.getUsername(), user.getEmail(), user.getRole().name()
            ));
        } catch (Exception ex) {
            log.error("Login failed for user {}: {}", identifier, ex.getMessage());
            return ResponseEntity.status(401).body("Invalid username/email or password");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody Map<String, String> request) {
        log.info("Received request to reset password for: {}", request.get(EMAIL));
        try {
            userService.initiatePasswordReset(request.get(EMAIL));
            log.debug("Password reset link sent to email: {}", request.get(EMAIL));
            return ResponseEntity.ok("Reset link sent to your email.");
        } catch (IllegalArgumentException ex) {
            log.error("Password reset failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> request) {
        log.info("Received request to reset password using token");
        try {
            userService.resetPassword(request.get("token"), request.get("password"));
            log.debug("Password reset successful for token: {}", request.get("token"));
            return ResponseEntity.ok("Password reset successful.");
        } catch (IllegalArgumentException ex) {
            log.error("Password reset failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/email-by-username")
    public ResponseEntity<Object> getEmailByUsername(@RequestParam("username") String username) {
        log.info("Received request to fetch email for username: {}", username);
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("User not found for username: {}", username);
            return ResponseEntity.badRequest().body(USER_NOT_FOUND_ERROR_MESSAGE);
        }
        User user = userOpt.get();
        if (user.isEnabled()) {
            log.debug("Email already verified for username: {}", username);
            return ResponseEntity.badRequest().body("Email already verified");
        }
        log.debug("Email retrieved for username {}: {}", username, user.getEmail());
        return ResponseEntity.ok(Map.of(EMAIL, user.getEmail()));
    }
}

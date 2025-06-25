package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AuthRequest;
import com.dishant.tasks.management.dto.AuthResponse;
import com.dishant.tasks.management.dto.RegisterRequest;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.service.CustomUserDetails;
import com.dishant.tasks.management.service.EmailService;
import com.dishant.tasks.management.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        log.info("Received request to register user: {}", request.getUsername());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        String token = UUID.randomUUID().toString();

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);
        user.setVerificationToken(token);

        userRepository.save(user);
        emailService.sendVerificationEmail(user);

        log.info("✅ User registered successfully: {} (Pending verification)", user.getUsername());

        // Issue JWT anyway (optional: you can decide to restrict login until email verified)
        String jwt = jwtService.generateToken(new CustomUserDetails(user));
        return ResponseEntity.ok(new AuthResponse(jwt, user.getUsername(), user.getEmail(), user.getRole().name()));
    }


    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired verification token");
        }

        User user = userOpt.get();
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        log.info("✅ Email verified for user: {}", user.getUsername());
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found");
        }

        User user = userOpt.get();

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Email already verified");
        }

        // Generate new token and save
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        // Resend email
        emailService.sendVerificationEmail(user);

        return ResponseEntity.ok("Verification email resent");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String identifier = request.getUsernameOrEmail();
        log.info("Received login request for: {}", identifier);

        try {
            Optional<User> userOpt = userRepository.findByEmail(identifier)
                    .or(() -> userRepository.findByUsername(identifier));

            if (userOpt.isEmpty()) {
                log.warn("Login failed: user not found for identifier '{}'", identifier);
                return ResponseEntity.status(401).body("Invalid username/email or password");
            }

            User u = userOpt.get();

            if (!u.isEnabled()) {
                log.warn("Login failed: user '{}' has not verified email", identifier);
                return ResponseEntity.status(401).body("Email not verified");
            }
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword()));

            CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();
            User user = customUserDetails.getUser();

            String token = jwtService.generateToken(customUserDetails);

            log.info("JWT generated successfully for user: {}", user.getUsername());

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name()
            ));
        } catch (Exception ex) {
            log.error("Login failed for '{}': {}", identifier, ex.getMessage());
            return ResponseEntity.status(401).body("Invalid username/email or password");
        }
    }

    @GetMapping("/email-by-username")
    public ResponseEntity<?> getEmailByUsername(@RequestParam("username") String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = userOpt.get();
        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Email already verified");
        }
        return ResponseEntity.ok(Map.of("email", user.getEmail()));
    }
}

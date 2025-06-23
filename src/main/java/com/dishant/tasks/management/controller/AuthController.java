package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AuthRequest;
import com.dishant.tasks.management.dto.AuthResponse;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        log.info("Received request to register user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Username already exists: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", request.getUsername());

        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Received request to login user: {}", request.getUsername());

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String token = jwtService.generateToken(auth.getPrincipal());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        log.info("JWT generated successfully for user: {}", request.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, user.getRole().name()));
    }
}

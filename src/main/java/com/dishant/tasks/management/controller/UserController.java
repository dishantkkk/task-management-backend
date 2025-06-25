package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.PasswordUpdateRequest;
import com.dishant.tasks.management.dto.UpdateProfileRequest;
import com.dishant.tasks.management.dto.UserResponse;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.utils.TaskHelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final TaskHelperUtil taskHelper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 🔐 Admin-only: List all users
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("Received request to get all users!");
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole().name()
                ))
                .toList();
    }

    /**
     * 👤 Authenticated user: Get own profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        log.info("Received request to get current profile");
        User user = taskHelper.getCurrentUser();
        log.info("Fetching profile for user: {}", user.getUsername());
        return ResponseEntity.ok(new UserResponse(user));
    }

    /**
     * 📝 Update name & email
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(@RequestBody UpdateProfileRequest request) {
        User user = taskHelper.getCurrentUser();
        user.setName(request.name());
        user.setEmail(request.email());
        userRepository.save(user);
        log.info("Profile updated for user: {}", user.getUsername());
        return ResponseEntity.ok("Profile updated successfully");
    }

    /**
     * 🔐 Update password with current password check
     */
    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody PasswordUpdateRequest request) {
        User user = taskHelper.getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Incorrect current password for user: {}", user.getUsername());
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getUsername());
        return ResponseEntity.ok("Password updated successfully");
    }
}

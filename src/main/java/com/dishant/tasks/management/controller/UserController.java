package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.UserResponse;
import com.dishant.tasks.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        log.info("Received request to get all users!");
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getId(), user.getUsername()))
                .toList();
    }
}

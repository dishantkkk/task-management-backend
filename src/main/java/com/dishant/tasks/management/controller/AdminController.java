package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AdminUserResponse;
import com.dishant.tasks.management.dto.UserRoleUpdateRequest;
import com.dishant.tasks.management.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/v1/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        log.info("Received request to get User with ID: {}", id);
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestBody UserRoleUpdateRequest request) {
        log.info("Received request to update the user role with id: {}", id);
        adminService.updateUserRole(id, request.getRole());
        return ResponseEntity.ok("User role updated successfully.");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        log.info("Received request to delete the user with id: {}", id);
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Received request to get the dashboard stats");
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}

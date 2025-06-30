package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AdminUserResponse;
import com.dishant.tasks.management.dto.UserRoleUpdateRequest;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static com.dishant.tasks.management.model.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById() {
        Long userId = 1L;
        AdminUserResponse mockResponse = new AdminUserResponse(userId, "John", "john@example.com", Role.USER, true);

        when(adminService.getUserById(userId)).thenReturn(mockResponse);

        ResponseEntity<AdminUserResponse> response = adminController.getUserById(userId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("john@example.com", response.getBody().getEmail());
        verify(adminService).getUserById(userId);
    }

    @Test
    void testUpdateUserRole() {
        Long userId = 1L;
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(ADMIN);

        ResponseEntity<String> response = adminController.updateUserRole(userId, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User role updated successfully.", response.getBody());
        verify(adminService).updateUserRole(userId, ADMIN);
    }

    @Test
    void testDeleteUser() {
        Long userId = 2L;

        ResponseEntity<String> response = adminController.deleteUser(userId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User deleted successfully.", response.getBody());
        verify(adminService).deleteUser(userId);
    }

    @Test
    void testGetDashboardStats() {
        Map<String, Object> mockStats = Map.of(
                "totalUsers", 10,
                "totalTasks", 50
        );

        when(adminService.getDashboardStats()).thenReturn(mockStats);

        ResponseEntity<Map<String, Object>> response = adminController.getDashboardStats();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(10, response.getBody().get("totalUsers"));
        verify(adminService).getDashboardStats();
    }
}

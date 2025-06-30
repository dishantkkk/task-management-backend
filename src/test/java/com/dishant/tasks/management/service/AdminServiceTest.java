package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.AdminUserResponse;
import com.dishant.tasks.management.exception.ResourceNotFoundException;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static com.dishant.tasks.management.constants.Constants.USER_NOT_FOUND_ERROR_MESSAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        taskRepository = mock(TaskRepository.class);
        adminService = new AdminService(userRepository, taskRepository);
    }

    @Test
    void getUserById_success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AdminUserResponse response = adminService.getUserById(userId);

        assertEquals(userId, response.getId());
        assertEquals("admin", response.getUsername());
        assertEquals("admin@example.com", response.getEmail());
        assertEquals(Role.ADMIN, response.getRole());
    }

    @Test
    void getUserById_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adminService.getUserById(99L));

        assertEquals(USER_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void updateUserRole_success() {
        Long userId = 2L;
        User user = new User();
        user.setId(userId);
        user.setRole(Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.updateUserRole(userId, Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adminService.updateUserRole(99L, Role.ADMIN));

        assertEquals(USER_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void deleteUser_success() {
        Long userId = 3L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.deleteUser(userId);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_userNotFound_throwsException() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> adminService.deleteUser(123L));

        assertEquals(USER_NOT_FOUND_ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void getDashboardStats_success() {
        when(userRepository.count()).thenReturn(100L);
        when(taskRepository.count()).thenReturn(200L);
        when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(80L);
        when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(70L);
        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(50L);

        Map<String, Object> stats = adminService.getDashboardStats();

        assertEquals(100L, stats.get("totalUsers"));
        assertEquals(200L, stats.get("totalTasks"));
        assertEquals(80L, stats.get("completedTasks"));
        assertEquals(70L, stats.get("pendingTasks"));
        assertEquals(50L, stats.get("inProgressTasks"));
    }
}

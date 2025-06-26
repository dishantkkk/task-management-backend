package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.PasswordUpdateRequest;
import com.dishant.tasks.management.dto.UpdateProfileRequest;
import com.dishant.tasks.management.dto.UserResponse;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.utils.TaskHelperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskHelperUtil taskHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .name("Dishant")
                .username("dishantkkk")
                .email("dishant@example.com")
                .role(Role.USER)
                .password("encodedPassword")
                .build();
    }

    @Test
    void testGetCurrentProfile() {
        when(taskHelper.getCurrentUser()).thenReturn(testUser);

        ResponseEntity<UserResponse> response = userController.getProfile();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Dishant", response.getBody().getName());
        verify(taskHelper, times(1)).getCurrentUser();
    }

    @Test
    void testGetAllUsers_AsAdmin() {
        User anotherUser = User.builder()
                .id(2L)
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));

        List<UserResponse> result = userController.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("Dishant", result.get(0).getName());
        assertEquals("Alice", result.get(1).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateProfile() {
        when(taskHelper.getCurrentUser()).thenReturn(testUser);

        UpdateProfileRequest updateRequest = new UpdateProfileRequest("Updated Name", "updated@email.com");

        ResponseEntity<String> response = userController.updateProfile(updateRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Profile updated successfully", response.getBody());
        verify(userRepository, times(1)).save(testUser);

        assertEquals("Updated Name", testUser.getName());
        assertEquals("updated@email.com", testUser.getEmail());
    }

    @Test
    void testUpdatePassword_Success() {
        when(taskHelper.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("oldpass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPassword");

        PasswordUpdateRequest request = new PasswordUpdateRequest("oldpass", "newpass");

        ResponseEntity<String> response = userController.updatePassword(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Password updated successfully", response.getBody());
        verify(userRepository, times(1)).save(testUser);
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void testUpdatePassword_IncorrectCurrentPassword() {
        when(taskHelper.getCurrentUser()).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        PasswordUpdateRequest request = new PasswordUpdateRequest("wrongpass", "newpass");

        ResponseEntity<String> response = userController.updatePassword(request);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Current password is incorrect", response.getBody());
        verify(userRepository, never()).save(any());
    }
}

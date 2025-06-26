package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AuthRequest;
import com.dishant.tasks.management.dto.AuthResponse;
import com.dishant.tasks.management.dto.RegisterRequest;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.security.CustomUserDetails;
import com.dishant.tasks.management.security.JwtService;
import com.dishant.tasks.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    private AuthRequest authRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authRequest = AuthRequest.builder()
                .usernameOrEmail("testuser")
                .password("testpass")
                .build();

        registerRequest = RegisterRequest.builder()
                .email("abc@mail.com")
                .name("abc")
                .username("abc")
                .password("testpass")
                .confirmPassword("testpass")
                .build();
    }

    @Test
    void register_Success() {
        User user = User.builder()
                .username("abc")
                .email("abc@mail.com")
                .role(Role.USER)
                .build();

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("email", "abc@mail.com");
        requestMap.put("name", "abc");
        requestMap.put("username", "abc");
        requestMap.put("password", "testpass");
        requestMap.put("confirmPassword", "testpass");

        when(userService.registerUser(requestMap)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.register(requestMap);

        assertEquals(200, response.getStatusCodeValue());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertEquals("abc", body.getUsername());
        assertEquals("abc@mail.com", body.getEmail());
        assertEquals("USER", body.getRole());
    }

    @Test
    void register_Failure() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("username", "abc");
        requestMap.put("email", "abc@mail.com");
        requestMap.put("name", "abc");
        requestMap.put("password", "testpass");
        requestMap.put("confirmPassword", "testpass");

        when(userService.registerUser(anyMap())).thenThrow(new IllegalArgumentException("Username already exists"));

        ResponseEntity<?> response = authController.register(requestMap);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username already exists", response.getBody());
    }

    @Test
    void login_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);
        user.setEnabled(true);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = mock(Authentication.class);

        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(user));
        when(authManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(authRequest);

        assertEquals(200, response.getStatusCodeValue());
        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("jwt-token", authResponse.getToken());
    }

    @Test
    void login_UnverifiedUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEnabled(false);

        when(userRepository.findByEmail("testuser")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.login(authRequest);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Email not verified", response.getBody());
    }

    @Test
    void verifyEmail_Success() {
        when(userService.verifyEmail("valid-token")).thenReturn(true);

        ResponseEntity<?> response = authController.verifyEmail("valid-token");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Email verified successfully. You can now log in.", response.getBody());
    }

    @Test
    void verifyEmail_InvalidToken() {
        when(userService.verifyEmail("invalid")).thenReturn(false);

        ResponseEntity<?> response = authController.verifyEmail("invalid");
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid or expired verification token", response.getBody());
    }

    @Test
    void resendVerification_Success() {
        Map<String, String> map = Map.of("email", "test@mail.com");
        doNothing().when(userService).resendVerification("test@mail.com");

        ResponseEntity<?> response = authController.resendVerification(map);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Verification email resent", response.getBody());
    }

    @Test
    void resendVerification_Failure() {
        Map<String, String> map = Map.of("email", "test@mail.com");
        doThrow(new IllegalArgumentException("Email already verified"))
                .when(userService).resendVerification("test@mail.com");

        ResponseEntity<?> response = authController.resendVerification(map);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email already verified", response.getBody());
    }

    @Test
    void forgotPassword_Success() {
        Map<String, String> map = Map.of("email", "abc@mail.com");
        doNothing().when(userService).initiatePasswordReset("abc@mail.com");

        ResponseEntity<?> response = authController.forgotPassword(map);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Reset link sent to your email.", response.getBody());
    }

    @Test
    void forgotPassword_UserNotFound() {
        Map<String, String> map = Map.of("email", "abc@mail.com");
        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).initiatePasswordReset("abc@mail.com");

        ResponseEntity<?> response = authController.forgotPassword(map);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void resetPassword_Success() {
        Map<String, String> map = Map.of("token", "abc123", "password", "newpass");
        doNothing().when(userService).resetPassword("abc123", "newpass");

        ResponseEntity<?> response = authController.resetPassword(map);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password reset successful.", response.getBody());
    }

    @Test
    void resetPassword_InvalidToken() {
        Map<String, String> map = Map.of("token", "abc123", "password", "newpass");
        doThrow(new IllegalArgumentException("Token expired"))
                .when(userService).resetPassword("abc123", "newpass");

        ResponseEntity<?> response = authController.resetPassword(map);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Token expired", response.getBody());
    }
}

package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.AuthRequest;
import com.dishant.tasks.management.dto.AuthResponse;
import com.dishant.tasks.management.dto.RegisterRequest;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.service.CustomUserDetails;
import com.dishant.tasks.management.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

    private AuthRequest request;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = AuthRequest.builder()
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
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testpass")).thenReturn("encodedpass");

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("User registered", response.getBody());
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(User.builder().build()));

        ResponseEntity<?> response = authController.register(registerRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Username already exists", response.getBody());
        verify(userRepo, never()).save(any());
    }

    @Test
    void login_Success() {
        AuthRequest request = new AuthRequest();
        request.setUsernameOrEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        Authentication mockAuth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(mockAuth.getPrincipal()).thenReturn(customUserDetails);

        when(jwtService.generateToken(customUserDetails)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());

        AuthResponse authResponse = (AuthResponse) response.getBody();
        assertNotNull(authResponse);
        assertEquals("jwt-token", authResponse.getToken());
        assertEquals("testuser", authResponse.getUsername());
        assertEquals("test@example.com", authResponse.getEmail());
        assertEquals("USER", authResponse.getRole());

        verify(authManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(customUserDetails);
    }

}

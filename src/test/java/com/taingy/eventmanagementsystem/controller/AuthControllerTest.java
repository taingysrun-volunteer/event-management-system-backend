package com.taingy.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.dto.UserResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.taingy.eventmanagementsystem.security.JwtUtil jwtUtil;

    @MockBean
    private com.taingy.eventmanagementsystem.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.taingy.eventmanagementsystem.repository.UserRepository userRepository;

    private AuthRequests.RegisterRequest registerRequest;
    private AuthRequests.LoginRequest loginRequest;
    private AuthRequests.ChangePasswordRequest changePasswordRequest;
    private AuthRequests.AuthResponse authResponse;
    private AuthRequests.RegisterResponse registerResponse;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthRequests.RegisterRequest(
                "testuser",
                "test@test.com",
                "John",
                "Doe",
                "password123"
        );

        loginRequest = new AuthRequests.LoginRequest(
                "testuser",
                "password123"
        );

        changePasswordRequest = new AuthRequests.ChangePasswordRequest(
                "oldPassword",
                "newPassword123"
        );

        userResponseDTO = UserResponseDTO.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        authResponse = new AuthRequests.AuthResponse(
                "jwt-token-here",
                userResponseDTO
        );

        registerResponse = new AuthRequests.RegisterResponse(
                "Registration successful. Please check your email for the verification code.",
                "test@test.com"
        );
    }

    @Test
    @WithAnonymousUser
    void register_Success() throws Exception {
        when(authService.register(any(AuthRequests.RegisterRequest.class)))
                .thenReturn(registerResponse);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for the verification code."))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(authService, times(1)).register(any(AuthRequests.RegisterRequest.class));
    }

    @Test
    @WithAnonymousUser
    void login_Success() throws Exception {
        when(authService.login(any(AuthRequests.LoginRequest.class)))
                .thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"));

        verify(authService, times(1)).login(any(AuthRequests.LoginRequest.class));
    }

    @Test
    @WithMockUser
    void changePassword_Success() throws Exception {
        doNothing().when(authService).changePassword(any(AuthRequests.ChangePasswordRequest.class));

        mockMvc.perform(post("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk());

        verify(authService, times(1)).changePassword(any(AuthRequests.ChangePasswordRequest.class));
    }
}

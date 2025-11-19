package com.taingy.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.eventmanagementsystem.dto.AuthRequests;
import com.taingy.eventmanagementsystem.dto.UserRequestDTO;
import com.taingy.eventmanagementsystem.dto.UserResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.mapper.UserMapper;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.taingy.eventmanagementsystem.security.JwtUtil jwtUtil;

    @MockBean
    private com.taingy.eventmanagementsystem.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.taingy.eventmanagementsystem.repository.UserRepository userRepository;

    private User testUser;
    private UserRequestDTO testUserRequestDTO;
    private UserResponseDTO testUserResponseDTO;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        testUserRequestDTO = UserRequestDTO.builder()
                .username("testuser")
                .email("test@test.com")
                .password("password123")
                .role(Role.USER)
                .build();

        testUserResponseDTO = UserResponseDTO.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .role(testUser.getRole())
                .createdAt(testUser.getCreatedAt())
                .updatedAt(testUser.getUpdatedAt())
                .build();
    }

    @Test
    @WithMockUser
    void createUser_Success() throws Exception {
        when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(testUser);
        when(userService.createUser(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(testUserResponseDTO);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    @WithMockUser
    void getAllUsers_Success() throws Exception {
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userService.getAllUsers(any(), any(), any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(testUserResponseDTO);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)))
                .andExpect(jsonPath("$.users[0].username").value("testuser"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @WithMockUser
    void getAllUsers_WithSearchAndRole() throws Exception {
        List<User> users = Collections.singletonList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userService.getAllUsers(eq("test"), eq(Role.USER), any(Pageable.class)))
                .thenReturn(userPage);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(testUserResponseDTO);

        mockMvc.perform(get("/api/users")
                        .param("search", "test")
                        .param("role", "USER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasSize(1)));

        verify(userService, times(1)).getAllUsers(eq("test"), eq(Role.USER), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        when(userService.getUserById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userMapper.toResponseDTO(testUser)).thenReturn(testUserResponseDTO);

        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void getUserById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_Success() throws Exception {
        when(userMapper.toEntity(any(UserRequestDTO.class))).thenReturn(testUser);
        when(userService.updateUser(eq(testUser.getId()), any(User.class))).thenReturn(testUser);
        when(userMapper.toResponseDTO(any(User.class))).thenReturn(testUserResponseDTO);

        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).updateUser(eq(testUser.getId()), any(User.class));
    }

    @Test
    @WithMockUser
    void deleteUser_Success() throws Exception {
        when(userService.getUserById(testUser.getId())).thenReturn(Optional.of(testUser));
        doNothing().when(userService).deleteUser(testUser.getId());

        mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(testUser.getId());
    }

    @Test
    @WithMockUser
    void deleteUser_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getUserById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/users/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void resetPassword_Success() throws Exception {
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        doNothing().when(userService).resetUserPassword(eq(testUser.getId()), eq("newPassword123"));

        AuthRequests.ResetPasswordRequest request = new AuthRequests.ResetPasswordRequest("newPassword123");

        mockMvc.perform(post("/api/users/{id}/reset-password", testUser.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService, times(1)).resetUserPassword(testUser.getId(), "newPassword123");
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void resetPassword_Forbidden_WhenNotAdmin() throws Exception {
        when(authService.getUserByUsername("user")).thenReturn(testUser);

        AuthRequests.ResetPasswordRequest request = new AuthRequests.ResetPasswordRequest("newPassword123");

        mockMvc.perform(post("/api/users/{id}/reset-password", testUser.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(userService, never()).resetUserPassword(any(), any());
    }
}

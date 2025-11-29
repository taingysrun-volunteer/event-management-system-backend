package com.taingy.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.eventmanagementsystem.dto.RegistrationRequestDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationResponseDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationUpdateDTO;
import com.taingy.eventmanagementsystem.enums.RegistrationStatus;
import com.taingy.eventmanagementsystem.mapper.RegistrationMapper;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.RegistrationService;
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

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private RegistrationMapper registrationMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.taingy.eventmanagementsystem.security.JwtUtil jwtUtil;

    @MockBean
    private com.taingy.eventmanagementsystem.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.taingy.eventmanagementsystem.repository.UserRepository userRepository;

    @MockBean
    private com.taingy.eventmanagementsystem.service.EmailService emailService;

    private Registration testRegistration;
    private RegistrationRequestDTO testRegistrationRequestDTO;
    private RegistrationResponseDTO testRegistrationResponseDTO;
    private RegistrationUpdateDTO testRegistrationUpdateDTO;
    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");

        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setTitle("Test Event");

        testRegistration = new Registration();
        testRegistration.setId(UUID.randomUUID());
        testRegistration.setUser(testUser);
        testRegistration.setEvent(testEvent);
        testRegistration.setStatus(RegistrationStatus.CONFIRMED);
        testRegistration.setNote("Test note");
        testRegistration.setCreatedAt(LocalDateTime.now());
        testRegistration.setUpdatedAt(LocalDateTime.now());

        testRegistrationRequestDTO = RegistrationRequestDTO.builder()
                .userId(testUser.getId())
                .eventId(testEvent.getId())
                .note("Test note")
                .build();

        testRegistrationResponseDTO = RegistrationResponseDTO.builder()
                .id(testRegistration.getId())
                .status(RegistrationStatus.CONFIRMED)
                .note("Test note")
                .createdAt(testRegistration.getCreatedAt())
                .updatedAt(testRegistration.getUpdatedAt())
                .build();

        testRegistrationUpdateDTO = RegistrationUpdateDTO.builder()
                .status(RegistrationStatus.CONFIRMED)
                .note("Updated note")
                .build();
    }

    @Test
    @WithMockUser
    void registerForEvent_Success() throws Exception {
        when(registrationService.registerAttendee(any(RegistrationRequestDTO.class)))
                .thenReturn(Optional.of(testRegistration));
        when(registrationMapper.toResponseDTO(any(Registration.class)))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(post("/api/registrations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegistrationRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testRegistration.getId().toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.note").value("Test note"));

        verify(registrationService, times(1)).registerAttendee(any(RegistrationRequestDTO.class));
    }

    @Test
    @WithMockUser
    void registerForEvent_BadRequest() throws Exception {
        when(registrationService.registerAttendee(any(RegistrationRequestDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/registrations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegistrationRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAllRegistrations_Success() throws Exception {
        List<Registration> registrations = Collections.singletonList(testRegistration);
        when(registrationService.getAllRegistrations()).thenReturn(registrations);
        when(registrationMapper.toResponseDTO(any(Registration.class)))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testRegistration.getId().toString()));
    }

    @Test
    @WithMockUser
    void getById_Success() throws Exception {
        when(registrationService.getRegistrationById(testRegistration.getId()))
                .thenReturn(Optional.of(testRegistration));
        when(registrationMapper.toResponseDTO(testRegistration))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(get("/api/registrations/{id}", testRegistration.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRegistration.getId().toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    void getById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(registrationService.getRegistrationById(nonExistentId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/registrations/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getByEvent_Success() throws Exception {
        List<Registration> registrations = Collections.singletonList(testRegistration);
        when(registrationService.getRegistrationsByEvent(testEvent.getId()))
                .thenReturn(registrations);
        when(registrationMapper.toResponseDTO(any(Registration.class)))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(get("/api/registrations/event/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testRegistration.getId().toString()));
    }

    @Test
    @WithMockUser
    void getByUser_Success() throws Exception {
        List<Registration> registrations = Collections.singletonList(testRegistration);
        Page<Registration> registrationPage = new PageImpl<>(registrations);

        when(registrationService.getRegistrationsByUser(eq(testUser.getId()), any(Pageable.class)))
                .thenReturn(registrationPage);
        when(registrationMapper.toResponseDTO(any(Registration.class)))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(get("/api/registrations/user/{userId}", testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrations", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser
    void cancelRegistration_Success() throws Exception {
        testRegistration.setStatus(RegistrationStatus.CANCELLED);
        testRegistrationResponseDTO.setStatus(RegistrationStatus.CANCELLED);

        when(registrationService.cancelRegistration(testRegistration.getId()))
                .thenReturn(Optional.of(testRegistration));
        when(registrationMapper.toResponseDTO(testRegistration))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(put("/api/registrations/{id}/cancel", testRegistration.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(registrationService, times(1)).cancelRegistration(testRegistration.getId());
    }

    @Test
    @WithMockUser
    void cancelRegistration_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(registrationService.cancelRegistration(nonExistentId))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/registrations/{id}/cancel", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateRegistration_Success() throws Exception {
        when(registrationService.updateRegistration(eq(testRegistration.getId()), any(RegistrationUpdateDTO.class)))
                .thenReturn(Optional.of(testRegistration));
        when(registrationMapper.toResponseDTO(testRegistration))
                .thenReturn(testRegistrationResponseDTO);

        mockMvc.perform(put("/api/registrations/{id}", testRegistration.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegistrationUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRegistration.getId().toString()));

        verify(registrationService, times(1))
                .updateRegistration(eq(testRegistration.getId()), any(RegistrationUpdateDTO.class));
    }

    @Test
    @WithMockUser
    void updateRegistration_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(registrationService.updateRegistration(eq(nonExistentId), any(RegistrationUpdateDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/registrations/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRegistrationUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void checkRegistration_Success_UserRegistered() throws Exception {
        when(authService.getUserByUsername("testuser")).thenReturn(testUser);
        when(registrationService.isUserRegisteredForEvent(testUser.getId(), testEvent.getId()))
                .thenReturn(true);

        mockMvc.perform(get("/api/registrations/check/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void checkRegistration_Success_UserNotRegistered() throws Exception {
        when(authService.getUserByUsername("testuser")).thenReturn(testUser);
        when(registrationService.isUserRegisteredForEvent(testUser.getId(), testEvent.getId()))
                .thenReturn(false);

        mockMvc.perform(get("/api/registrations/check/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(false));
    }
}

package com.taingy.eventmanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taingy.eventmanagementsystem.dto.EventRequestDTO;
import com.taingy.eventmanagementsystem.dto.EventResponseDTO;
import com.taingy.eventmanagementsystem.enums.EventStatus;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.mapper.EventMapper;
import com.taingy.eventmanagementsystem.mapper.RegistrationMapper;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.CategoryService;
import com.taingy.eventmanagementsystem.service.EventService;
import com.taingy.eventmanagementsystem.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private AuthService authService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private EventMapper eventMapper;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private RegistrationMapper registrationMapper;

    @MockBean
    private com.taingy.eventmanagementsystem.security.JwtUtil jwtUtil;

    @MockBean
    private com.taingy.eventmanagementsystem.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.taingy.eventmanagementsystem.repository.UserRepository userRepository;

    private Event testEvent;
    private EventRequestDTO testEventRequestDTO;
    private EventResponseDTO testEventResponseDTO;
    private User adminUser;
    private User regularUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Setup test users
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(UUID.randomUUID());
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setRole(Role.USER);

        // Setup test category
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Technology");

        // Setup test event
        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setEventDate(LocalDate.now().plusDays(7));
        testEvent.setStartTime(LocalDateTime.now().plusDays(7));
        testEvent.setEndTime(LocalDateTime.now().plusDays(7).plusHours(2));
        testEvent.setPrice(50.0);
        testEvent.setCapacity(100);
        testEvent.setStatus(EventStatus.ACTIVE);
        testEvent.setOrganizer(adminUser);
        testEvent.setCategory(testCategory);
        testEvent.setCreatedAt(LocalDateTime.now());
        testEvent.setUpdatedAt(LocalDateTime.now());

        // Setup test DTOs
        testEventRequestDTO = EventRequestDTO.builder()
                .title("Test Event")
                .description("Test Description")
                .location("Test Location")
                .eventDate(LocalDate.now().plusDays(7))
                .startTime(LocalDateTime.now().plusDays(7))
                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
                .price(50.0)
                .capacity(100)
                .status(EventStatus.ACTIVE)
                .categoryId(1)
                .build();

        testEventResponseDTO = EventResponseDTO.builder()
                .id(testEvent.getId())
                .title(testEvent.getTitle())
                .description(testEvent.getDescription())
                .location(testEvent.getLocation())
                .eventDate(testEvent.getEventDate())
                .startTime(testEvent.getStartTime())
                .endTime(testEvent.getEndTime())
                .price(testEvent.getPrice())
                .capacity(testEvent.getCapacity())
                .availableSeats(100)
                .status(testEvent.getStatus())
                .createdAt(testEvent.getCreatedAt())
                .updatedAt(testEvent.getUpdatedAt())
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createEvent_Success() throws Exception {
        // Arrange
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(testCategory));
        when(eventMapper.toEntity(any(EventRequestDTO.class))).thenReturn(testEvent);
        when(eventService.saveEvent(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponseDTO(any(Event.class))).thenReturn(testEventResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.capacity").value(100))
                .andExpect(jsonPath("$.availableSeats").value(100));

        verify(eventService, times(1)).saveEvent(any(Event.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createEvent_Forbidden_WhenNotAdmin() throws Exception {
        // Arrange
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).saveEvent(any(Event.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createEvent_BadRequest_WhenTitleIsEmpty() throws Exception {
        // Arrange
        testEventRequestDTO.setTitle("");
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).saveEvent(any(Event.class));
    }

    @Test
    @WithMockUser
    void getAllEvents_Success() throws Exception {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);

        when(eventService.getAllEvents(any(Pageable.class))).thenReturn(eventPage);
        when(eventMapper.toResponseDTO(any(Event.class), anyBoolean())).thenReturn(testEventResponseDTO);
        when(registrationService.getRegisteredEventIdsForUser(any())).thenReturn(new HashSet<>());

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].title").value("Test Event"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser
    void getAllEvents_WithSearchAndCategory() throws Exception {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        Page<Event> eventPage = new PageImpl<>(events);

        when(eventService.searchEvents(eq("Test"), eq(1), any(Pageable.class))).thenReturn(eventPage);
        when(eventMapper.toResponseDTO(any(Event.class), anyBoolean())).thenReturn(testEventResponseDTO);
        when(registrationService.getRegisteredEventIdsForUser(any())).thenReturn(new HashSet<>());

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("search", "Test")
                        .param("categoryId", "1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].title").value("Test Event"));

        verify(eventService, times(1)).searchEvents(eq("Test"), eq(1), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void getEventById_Success() throws Exception {
        // Arrange
        when(eventService.getEventById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        when(eventMapper.toResponseDTO(testEvent)).thenReturn(testEventResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService, times(1)).getEventById(testEvent.getId());
    }

    @Test
    @WithMockUser
    void getEventById_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(eventService.getEventById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateEvent_Success() throws Exception {
        // Arrange
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(eventService.getEventById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        when(categoryService.getCategoryById(1)).thenReturn(Optional.of(testCategory));
        when(eventService.saveEvent(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponseDTO(any(Event.class))).thenReturn(testEventResponseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventMapper, times(1)).updateEntityFromDTO(any(Event.class), any(EventRequestDTO.class));
        verify(eventService, times(1)).saveEvent(any(Event.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateEvent_Forbidden_WhenNotAdmin() throws Exception {
        // Arrange
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).saveEvent(any(Event.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateEvent_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(eventService.getEventById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/events/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteEvent_Success() throws Exception {
        // Arrange
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(eventService.getEventById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        doNothing().when(eventService).deleteEvent(testEvent.getId());

        // Act & Assert
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent(testEvent.getId());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteEvent_Forbidden_WhenNotAdmin() throws Exception {
        // Arrange
        when(authService.getUserByUsername("user")).thenReturn(regularUser);

        // Act & Assert
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(eventService, never()).deleteEvent(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteEvent_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(authService.getUserByUsername("admin")).thenReturn(adminUser);
        when(eventService.getEventById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/events/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getEventRegistrations_Success() throws Exception {
        // Arrange
        when(eventService.getEventById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        Page<com.taingy.eventmanagementsystem.model.Registration> registrationPage =
                new PageImpl<>(new ArrayList<>());
        when(registrationService.getRegistrationsByEvent(eq(testEvent.getId()), any(Pageable.class)))
                .thenReturn(registrationPage);

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/registrations", testEvent.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrations").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser
    void getEventRegistrations_EventNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(eventService.getEventById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/events/{id}/registrations", nonExistentId))
                .andExpect(status().isNotFound());
    }
}

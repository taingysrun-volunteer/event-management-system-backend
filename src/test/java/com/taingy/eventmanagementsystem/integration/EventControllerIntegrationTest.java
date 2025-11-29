package com.taingy.eventmanagementsystem.integration;

import com.taingy.eventmanagementsystem.dto.EventRequestDTO;
import com.taingy.eventmanagementsystem.enums.EventStatus;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.CategoryRepository;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EventController
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@Transactional
class EventControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User regularUser;
    private Category testCategory;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Clean up
        eventRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test users
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPasswordHash(passwordEncoder.encode("password123"));
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setPasswordHash(passwordEncoder.encode("password123"));
        regularUser.setRole(Role.USER);
        regularUser = userRepository.save(regularUser);

        // Create test category
        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setDescription("Technology events");
        testCategory = categoryRepository.save(testCategory);

        // Create test event
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setEventDate(LocalDate.now().plusDays(7));
        testEvent.setStartTime(LocalDateTime.now().plusDays(7));
        testEvent.setEndTime(LocalDateTime.now().plusDays(7).plusHours(2));
        testEvent.setPrice(50.0);
        testEvent.setCapacity(100);
        testEvent.setStatus(EventStatus.ACTIVE);
        testEvent.setCategory(testCategory);
        testEvent.setOrganizer(adminUser);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createEvent_Success_WhenAdmin() throws Exception {
        EventRequestDTO eventRequest = EventRequestDTO.builder()
                .title("New Event")
                .description("New Description")
                .location("New Location")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .price(75.0)
                .capacity(150)
                .status(EventStatus.ACTIVE)
                .categoryId(testCategory.getId())
                .build();

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Event"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.location").value("New Location"))
                .andExpect(jsonPath("$.price").value(75.0))
                .andExpect(jsonPath("$.capacity").value(150));

        // Verify data in database
        long count = eventRepository.count();
        assert count == 2; // Initial event + new event
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createEvent_Forbidden_WhenNotAdmin() throws Exception {
        EventRequestDTO eventRequest = EventRequestDTO.builder()
                .title("New Event")
                .description("New Description")
                .location("New Location")
                .eventDate(LocalDate.now().plusDays(10))
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(3))
                .price(75.0)
                .capacity(150)
                .status(EventStatus.ACTIVE)
                .categoryId(testCategory.getId())
                .build();

        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isForbidden());

        // Verify no new event was created
        long count = eventRepository.count();
        assert count == 1; // Only initial event
    }

    @Test
    @WithMockUser
    void getAllEvents_Success() throws Exception {
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
    void getEventById_Success() throws Exception {
        mockMvc.perform(get("/api/events/{id}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    @WithMockUser
    void getEventById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/events/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateEvent_Success_WhenAdmin() throws Exception {
        EventRequestDTO updateRequest = EventRequestDTO.builder()
                .title("Updated Event")
                .description("Updated Description")
                .location("Updated Location")
                .eventDate(LocalDate.now().plusDays(14))
                .startTime(LocalDateTime.now().plusDays(14))
                .endTime(LocalDateTime.now().plusDays(14).plusHours(4))
                .price(100.0)
                .capacity(200)
                .status(EventStatus.ACTIVE)
                .categoryId(testCategory.getId())
                .build();

        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.price").value(100.0));

        // Verify update in database
        Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assert updatedEvent.getTitle().equals("Updated Event");
        assert updatedEvent.getPrice().equals(100.0);
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void updateEvent_Forbidden_WhenNotAdmin() throws Exception {
        EventRequestDTO updateRequest = EventRequestDTO.builder()
                .title("Updated Event")
                .description("Updated Description")
                .location("Updated Location")
                .eventDate(LocalDate.now().plusDays(14))
                .startTime(LocalDateTime.now().plusDays(14))
                .endTime(LocalDateTime.now().plusDays(14).plusHours(4))
                .price(100.0)
                .capacity(200)
                .status(EventStatus.ACTIVE)
                .categoryId(testCategory.getId())
                .build();

        mockMvc.perform(put("/api/events/{id}", testEvent.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // Verify no update in database
        Event unchangedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
        assert unchangedEvent.getTitle().equals("Test Event");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteEvent_Success_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify deletion in database
        assert eventRepository.findById(testEvent.getId()).isEmpty();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void deleteEvent_Forbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Verify event still exists in database
        assert eventRepository.findById(testEvent.getId()).isPresent();
    }

    @Test
    @WithMockUser
    void searchEvents_Success() throws Exception {
        // Create additional event for search testing
        Event searchableEvent = new Event();
        searchableEvent.setTitle("Spring Boot Workshop");
        searchableEvent.setDescription("Learn Spring Boot");
        searchableEvent.setLocation("Online");
        searchableEvent.setEventDate(LocalDate.now().plusDays(5));
        searchableEvent.setStartTime(LocalDateTime.now().plusDays(5));
        searchableEvent.setEndTime(LocalDateTime.now().plusDays(5).plusHours(3));
        searchableEvent.setPrice(0.0);
        searchableEvent.setCapacity(50);
        searchableEvent.setStatus(EventStatus.ACTIVE);
        searchableEvent.setCategory(testCategory);
        searchableEvent.setOrganizer(adminUser);
        eventRepository.save(searchableEvent);

        mockMvc.perform(get("/api/events")
                        .param("search", "Spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].title").value("Spring Boot Workshop"));
    }

    @Test
    @WithMockUser
    void getEventsByCategory_Success() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("categoryId", testCategory.getId().toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].title").value("Test Event"));
    }
}

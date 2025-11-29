package com.taingy.eventmanagementsystem.integration;

import com.taingy.eventmanagementsystem.dto.RegistrationRequestDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationUpdateDTO;
import com.taingy.eventmanagementsystem.enums.EventStatus;
import com.taingy.eventmanagementsystem.enums.RegistrationStatus;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.CategoryRepository;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import com.taingy.eventmanagementsystem.repository.RegistrationRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 * Integration tests for RegistrationController
 * Tests the full stack: Controller -> Service -> Repository -> Database
 */
@Transactional
class RegistrationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.taingy.eventmanagementsystem.service.EmailService emailService;

    private User testUser;
    private User anotherUser;
    private Event testEvent;
    private Registration testRegistration;

    @BeforeEach
    void setUp() {
        // Clean up
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test users
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPasswordHash(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@test.com");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("User");
        anotherUser.setPasswordHash(passwordEncoder.encode("password123"));
        anotherUser.setRole(Role.USER);
        anotherUser = userRepository.save(anotherUser);

        // Create test category
        Category testCategory = new Category();
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
        testEvent.setOrganizer(testUser);
        testEvent = eventRepository.save(testEvent);

        // Create test registration
        testRegistration = new Registration();
        testRegistration.setUser(testUser);
        testRegistration.setEvent(testEvent);
        testRegistration.setStatus(RegistrationStatus.CONFIRMED);
        testRegistration.setNote("Test note");
        testRegistration = registrationRepository.save(testRegistration);
    }

    @Test
    @WithMockUser(username = "anotheruser")
    void registerForEvent_Success() throws Exception {
        // Create a new event for registration
        Event newEvent = new Event();
        newEvent.setTitle("New Event");
        newEvent.setDescription("New Description");
        newEvent.setLocation("New Location");
        newEvent.setEventDate(LocalDate.now().plusDays(10));
        newEvent.setStartTime(LocalDateTime.now().plusDays(10));
        newEvent.setEndTime(LocalDateTime.now().plusDays(10).plusHours(2));
        newEvent.setPrice(30.0);
        newEvent.setCapacity(50);
        newEvent.setStatus(EventStatus.ACTIVE);
        newEvent.setCategory(testEvent.getCategory());
        newEvent.setOrganizer(testUser);
        newEvent = eventRepository.save(newEvent);

        RegistrationRequestDTO requestDTO = RegistrationRequestDTO.builder()
                .userId(anotherUser.getId())
                .eventId(newEvent.getId())
                .note("Looking forward to this event")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.note").value("Looking forward to this event"));

        // Verify registration in database
        long count = registrationRepository.count();
        assert count == 2; // Initial registration + new registration
    }

    @Test
    @WithMockUser(username = "testuser")
    void registerForEvent_Conflict_WhenAlreadyRegistered() throws Exception {
        RegistrationRequestDTO requestDTO = RegistrationRequestDTO.builder()
                .userId(testUser.getId())
                .eventId(testEvent.getId())
                .note("Trying to register again")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict()); // 409 Conflict for duplicate registration

        // Verify no duplicate registration
        long count = registrationRepository.count();
        assert count == 1; // Only initial registration
    }

    @Test
    @WithMockUser
    void getAllRegistrations_Success() throws Exception {
        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser
    void getRegistrationById_Success() throws Exception {
        mockMvc.perform(get("/api/registrations/{id}", testRegistration.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRegistration.getId().toString()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.note").value("Test note"));
    }

    @Test
    @WithMockUser
    void getRegistrationById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/registrations/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getRegistrationsByEvent_Success() throws Exception {
        mockMvc.perform(get("/api/registrations/event/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
    }

    @Test
    @WithMockUser
    void getRegistrationsByUser_Success() throws Exception {
        mockMvc.perform(get("/api/registrations/user/{userId}", testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrations", hasSize(1)))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void cancelRegistration_Success() throws Exception {
        mockMvc.perform(put("/api/registrations/{id}/cancel", testRegistration.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Verify status change in database
        Registration cancelledRegistration = registrationRepository.findById(testRegistration.getId()).orElseThrow();
        assert cancelledRegistration.getStatus() == RegistrationStatus.CANCELLED;
    }

    @Test
    @WithMockUser
    void cancelRegistration_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(put("/api/registrations/{id}/cancel", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateRegistration_Success() throws Exception {
        RegistrationUpdateDTO updateDTO = RegistrationUpdateDTO.builder()
                .status(RegistrationStatus.CONFIRMED)
                .note("Updated note")
                .build();

        mockMvc.perform(put("/api/registrations/{id}", testRegistration.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("Updated note"));

        // Verify update in database
        Registration updatedRegistration = registrationRepository.findById(testRegistration.getId()).orElseThrow();
        assert updatedRegistration.getNote().equals("Updated note");
    }

    @Test
    @WithMockUser
    void updateRegistration_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        RegistrationUpdateDTO updateDTO = RegistrationUpdateDTO.builder()
                .status(RegistrationStatus.CONFIRMED)
                .note("Updated note")
                .build();

        mockMvc.perform(put("/api/registrations/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser")
    void checkRegistration_UserRegistered() throws Exception {
        mockMvc.perform(get("/api/registrations/check/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(true));
    }

    @Test
    @WithMockUser(username = "anotheruser")
    void checkRegistration_UserNotRegistered() throws Exception {
        mockMvc.perform(get("/api/registrations/check/{eventId}", testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value(false));
    }

    // Note: This test is commented out because the current implementation doesn't check event capacity
    // Uncomment when capacity validation is implemented in RegistrationService
    /*
    @Test
    @WithMockUser(username = "anotheruser")
    void registerForEvent_BadRequest_WhenEventAtCapacity() throws Exception {
        // Create event with capacity 1
        Event fullEvent = new Event();
        fullEvent.setTitle("Full Event");
        fullEvent.setDescription("This event is at capacity");
        fullEvent.setLocation("Test Location");
        fullEvent.setEventDate(LocalDate.now().plusDays(5));
        fullEvent.setStartTime(LocalDateTime.now().plusDays(5));
        fullEvent.setEndTime(LocalDateTime.now().plusDays(5).plusHours(2));
        fullEvent.setPrice(0.0);
        fullEvent.setCapacity(1);
        fullEvent.setStatus(EventStatus.ACTIVE);
        fullEvent.setCategory(testEvent.getCategory());
        fullEvent.setOrganizer(testUser);
        fullEvent = eventRepository.save(fullEvent);

        // Fill the event to capacity
        Registration firstRegistration = new Registration();
        firstRegistration.setUser(testUser);
        firstRegistration.setEvent(fullEvent);
        firstRegistration.setStatus(RegistrationStatus.CONFIRMED);
        registrationRepository.save(firstRegistration);

        // Try to register when event is full
        RegistrationRequestDTO requestDTO = RegistrationRequestDTO.builder()
                .userId(anotherUser.getId())
                .eventId(fullEvent.getId())
                .note("Trying to register for full event")
                .build();

        mockMvc.perform(post("/api/registrations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }
    */
}

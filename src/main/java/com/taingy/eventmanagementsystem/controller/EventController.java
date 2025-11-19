package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.EventRequestDTO;
import com.taingy.eventmanagementsystem.dto.EventResponseDTO;
import com.taingy.eventmanagementsystem.enums.Role;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.ForbiddenException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.exception.UnauthorizedException;
import com.taingy.eventmanagementsystem.mapper.EventMapper;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.CategoryService;
import com.taingy.eventmanagementsystem.service.EventService;
import com.taingy.eventmanagementsystem.util.AuthUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final AuthService authService;
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final com.taingy.eventmanagementsystem.service.RegistrationService registrationService;
    private final com.taingy.eventmanagementsystem.mapper.RegistrationMapper registrationMapper;

    public EventController(EventService eventService, AuthService authService,
                          CategoryService categoryService, EventMapper eventMapper,
                          com.taingy.eventmanagementsystem.service.RegistrationService registrationService,
                          com.taingy.eventmanagementsystem.mapper.RegistrationMapper registrationMapper) {
        this.eventService = eventService;
        this.authService = authService;
        this.categoryService = categoryService;
        this.eventMapper = eventMapper;
        this.registrationService = registrationService;
        this.registrationMapper = registrationMapper;
    }

    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@RequestBody EventRequestDTO eventRequestDTO) {
        if (eventRequestDTO.getTitle() == null || eventRequestDTO.getTitle().isBlank()) {
            throw new BadRequestException("Event title is required");
        }

        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User organizer = authService.getUserByUsername(username);
        if (organizer == null) {
            throw new UnauthorizedException("User not found");
        }

        if (organizer.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can create events");
        }

        Event event = eventMapper.toEntity(eventRequestDTO);
        event.setOrganizer(organizer);

        if (eventRequestDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(eventRequestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", eventRequestDTO.getCategoryId()));
            event.setCategory(category);
        }

        Event saved = eventService.saveEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventMapper.toResponseDTO(saved));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEvents(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Event> eventPage = (search != null || categoryId != null) ? eventService.searchEvents(search, categoryId, pageable) : eventService.getAllEvents(pageable);

        // Get current user and their registered event IDs
        String username = AuthUtil.getCurrentUsername();
        Set<UUID> registeredEventIds = new HashSet<>();
        if (username != null) {
            User currentUser = authService.getUserByUsername(username);
            if (currentUser != null) {
                registeredEventIds = registrationService.getRegisteredEventIdsForUser(currentUser.getId());
            }
        }

        // Map events to DTOs with registration status
        final Set<UUID> finalRegisteredEventIds = registeredEventIds;
        List<EventResponseDTO> events = eventPage.getContent().stream()
                .map(event -> eventMapper.toResponseDTO(event, finalRegisteredEventIds.contains(event.getId())))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("events", events);
        response.put("currentPage", eventPage.getNumber());
        response.put("totalItems", eventPage.getTotalElements());
        response.put("totalPages", eventPage.getTotalPages());
        response.put("pageSize", eventPage.getSize());
        response.put("hasNext", eventPage.hasNext());
        response.put("hasPrevious", eventPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable UUID id) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
        return ResponseEntity.ok(eventMapper.toResponseDTO(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable UUID id, @RequestBody EventRequestDTO eventRequestDTO) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can update events");
        }

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        eventMapper.updateEntityFromDTO(event, eventRequestDTO);

        if (eventRequestDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(eventRequestDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", eventRequestDTO.getCategoryId()));
            event.setCategory(category);
        }

        Event saved = eventService.saveEvent(event);
        return ResponseEntity.ok(eventMapper.toResponseDTO(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can delete events");
        }

        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        eventService.deleteEvent(event.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/registrations")
    public ResponseEntity<Map<String, Object>> getEventRegistrations(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<com.taingy.eventmanagementsystem.model.Registration> registrationPage =
                registrationService.getRegistrationsByEvent(id, pageable);

        List<com.taingy.eventmanagementsystem.dto.RegistrationResponseDTO> registrations =
                registrationPage.getContent().stream()
                .map(registrationMapper::toResponseDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("registrations", registrations);
        response.put("currentPage", registrationPage.getNumber());
        response.put("totalItems", registrationPage.getTotalElements());
        response.put("totalPages", registrationPage.getTotalPages());
        response.put("pageSize", registrationPage.getSize());
        response.put("hasNext", registrationPage.hasNext());
        response.put("hasPrevious", registrationPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

}


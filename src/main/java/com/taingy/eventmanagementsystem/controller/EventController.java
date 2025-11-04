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

    public EventController(EventService eventService, AuthService authService,
                          CategoryService categoryService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.authService = authService;
        this.categoryService = categoryService;
        this.eventMapper = eventMapper;
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
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        List<EventResponseDTO> events = eventService.getAllEvents().stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
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

    @GetMapping("/search")
    public ResponseEntity<List<EventResponseDTO>> searchEvents(@RequestParam("q") String keyword) {
        List<EventResponseDTO> events = eventService.searchEvents(keyword).stream()
                .map(eventMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

}


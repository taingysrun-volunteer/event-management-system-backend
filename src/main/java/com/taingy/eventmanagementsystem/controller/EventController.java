package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.EventService;
import com.taingy.eventmanagementsystem.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final AuthService authService;

    public EventController(EventService eventService, AuthService authService) {
        this.eventService = eventService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        User organizer = authService.getUserByUsername(username);
        event.setOrganizer(organizer);
        Event saved = eventService.saveEvent(event);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable UUID id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable UUID id, @RequestBody Event updatedEvent) {
        return eventService.getEventById(id)
                .map(event -> {
                    event.setTitle(updatedEvent.getTitle());
                    event.setDescription(updatedEvent.getDescription());
                    event.setLocation(updatedEvent.getLocation());
                    event.setStartTime(updatedEvent.getStartTime());
                    event.setEndTime(updatedEvent.getEndTime());
                    event.setCategory(updatedEvent.getCategory());
                    Event saved = eventService.saveEvent(event);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        if (eventService.getEventById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam("q") String keyword) {
        return ResponseEntity.ok(eventService.searchEvents(keyword));
    }

}


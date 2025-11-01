package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<Registration> registerForEvent(
            @PathVariable UUID eventId,
            @RequestParam(name = "userId") UUID userId
    ) {
        return registrationService.registerAttendee(eventId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<Registration>> getAllRegistrations() {
        return ResponseEntity.ok(registrationService.getAllRegistrations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Registration> getById(@PathVariable UUID id) {
        return registrationService.getRegistrationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Registration>> getByEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(registrationService.getRegistrationsByEvent(eventId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Registration> cancelRegistration(@PathVariable UUID id) {
        return registrationService.cancelRegistration(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

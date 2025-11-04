package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.RegistrationResponseDTO;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.mapper.RegistrationMapper;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationMapper registrationMapper;

    public RegistrationController(RegistrationService registrationService, RegistrationMapper registrationMapper) {
        this.registrationService = registrationService;
        this.registrationMapper = registrationMapper;
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<RegistrationResponseDTO> registerForEvent(
            @PathVariable UUID eventId,
            @RequestParam(name = "userId") UUID userId
    ) {
        Registration registration = registrationService.registerAttendee(eventId, userId)
                .orElseThrow(() -> new BadRequestException("Unable to register for event"));
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationMapper.toResponseDTO(registration));
    }

    @GetMapping
    public ResponseEntity<List<RegistrationResponseDTO>> getAllRegistrations() {
        List<RegistrationResponseDTO> registrations = registrationService.getAllRegistrations().stream()
                .map(registrationMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDTO> getById(@PathVariable UUID id) {
        Registration registration = registrationService.getRegistrationById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
        return ResponseEntity.ok(registrationMapper.toResponseDTO(registration));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<RegistrationResponseDTO>> getByEvent(@PathVariable UUID eventId) {
        List<RegistrationResponseDTO> registrations = registrationService.getRegistrationsByEvent(eventId).stream()
                .map(registrationMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(registrations);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<RegistrationResponseDTO> cancelRegistration(@PathVariable UUID id) {
        Registration registration = registrationService.cancelRegistration(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
        return ResponseEntity.ok(registrationMapper.toResponseDTO(registration));
    }

}

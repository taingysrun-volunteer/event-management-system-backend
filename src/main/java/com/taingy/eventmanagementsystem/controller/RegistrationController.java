package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.RegistrationRequestDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationResponseDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationUpdateDTO;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.exception.UnauthorizedException;
import com.taingy.eventmanagementsystem.mapper.RegistrationMapper;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.service.AuthService;
import com.taingy.eventmanagementsystem.service.RegistrationService;
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
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final RegistrationMapper registrationMapper;
    private final AuthService authService;

    public RegistrationController(RegistrationService registrationService, RegistrationMapper registrationMapper,
                                  AuthService authService) {
        this.registrationService = registrationService;
        this.registrationMapper = registrationMapper;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDTO> registerForEvent(
            @RequestBody RegistrationRequestDTO request
    ) {
        Registration registration = registrationService.registerAttendee(request)
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Registration> registrationPage = registrationService.getRegistrationsByUser(userId, pageable);

        List<RegistrationResponseDTO> registrations = registrationPage.getContent().stream()
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

    @PutMapping("/{id}/cancel")
    public ResponseEntity<RegistrationResponseDTO> cancelRegistration(@PathVariable UUID id) {
        Registration registration = registrationService.cancelRegistration(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
        return ResponseEntity.ok(registrationMapper.toResponseDTO(registration));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDTO> updateRegistration(
            @PathVariable UUID id,
            @RequestBody RegistrationUpdateDTO updateDTO
    ) {
        Registration registration = registrationService.updateRegistration(id, updateDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));
        return ResponseEntity.ok(registrationMapper.toResponseDTO(registration));
    }

    @GetMapping("/check/{eventId}")
    public ResponseEntity<Map<String, Boolean>> checkRegistration(@PathVariable UUID eventId) {
        String username = AuthUtil.getCurrentUsername();
        if (username == null) {
            throw new UnauthorizedException("Authentication required");
        }

        User currentUser = authService.getUserByUsername(username);
        if (currentUser == null) {
            throw new UnauthorizedException("User not found");
        }

        boolean isRegistered = registrationService.isUserRegisteredForEvent(currentUser.getId(), eventId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("registered", isRegistered);

        return ResponseEntity.ok(response);
    }

}

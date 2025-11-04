package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.enums.RegistrationStatus;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.DuplicateResourceException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import com.taingy.eventmanagementsystem.repository.RegistrationRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public Optional<Registration> registerAttendee(UUID eventId, UUID userId) {
        // Check if event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user is already registered for this event
        Optional<Registration> existingRegistration = registrationRepository.findByUserAndEvent(user, event);
        if (existingRegistration.isPresent()) {
            Registration existing = existingRegistration.get();
            // If registration is cancelled, allow re-registration
            if (existing.getStatus() == RegistrationStatus.CANCELLED) {
                existing.setStatus(RegistrationStatus.CONFIRMED);
                return Optional.of(registrationRepository.save(existing));
            }
            // Otherwise, throw duplicate exception
            throw new DuplicateResourceException("User is already registered for this event");
        }

        // Create new registration
        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setStatus(RegistrationStatus.CONFIRMED);

        return Optional.of(registrationRepository.save(registration));
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public List<Registration> getRegistrationsByEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        return registrationRepository.findByEvent(event);
    }

    public Optional<Registration> getRegistrationById(UUID id) {
        return registrationRepository.findById(id);
    }

    public Optional<Registration> cancelRegistration(UUID id) {
        Optional<Registration> regOpt = registrationRepository.findById(id);
        if (regOpt.isPresent()) {
            Registration reg = regOpt.get();
            reg.setStatus(RegistrationStatus.CANCELLED);
            return Optional.of(registrationRepository.save(reg));
        }
        return Optional.empty();
    }

}


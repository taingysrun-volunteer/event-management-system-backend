package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.dto.RegistrationRequestDTO;
import com.taingy.eventmanagementsystem.dto.RegistrationUpdateDTO;
import com.taingy.eventmanagementsystem.enums.RegistrationStatus;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.DuplicateResourceException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Notification;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import com.taingy.eventmanagementsystem.repository.NotificationRepository;
import com.taingy.eventmanagementsystem.repository.RegistrationRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    public RegistrationService(RegistrationRepository registrationRepository, EventRepository eventRepository,
                              UserRepository userRepository, EmailService emailService,
                              NotificationRepository notificationRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
    }

    public Optional<Registration> registerAttendee(RegistrationRequestDTO request) {
        // Check if event exists
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));

        // Check if user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        // Check if user is already registered for this event
        Optional<Registration> existingRegistration = registrationRepository.findByUserAndEvent(user, event);
        if (existingRegistration.isPresent()) {
            Registration existing = existingRegistration.get();
            // If registration is cancelled, allow re-registration
            if (existing.getStatus() == RegistrationStatus.CANCELLED) {
                existing.setStatus(RegistrationStatus.CONFIRMED);
                existing.setNote(request.getNote());
                Registration savedRegistration = registrationRepository.save(existing);

                // Send confirmation email
                emailService.sendRegistrationConfirmation(savedRegistration);

                // Save notification
                saveNotification(user, event, "You have successfully registered for event: " + event.getTitle(), "REGISTRATION");

                return Optional.of(savedRegistration);
            }
            // Otherwise, throw duplicate exception
            throw new DuplicateResourceException("User is already registered for this event");
        }

        // Create new registration
        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registration.setNote(request.getNote());

        Registration savedRegistration = registrationRepository.save(registration);

        // Send confirmation email
        emailService.sendRegistrationConfirmation(savedRegistration);

        // Save notification
        saveNotification(user, event, "You have successfully registered for event: " + event.getTitle(), "REGISTRATION");

        return Optional.of(savedRegistration);
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    public List<Registration> getRegistrationsByEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        return registrationRepository.findByEvent(event);
    }

    public Page<Registration> getRegistrationsByEvent(UUID eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));
        return registrationRepository.findByEvent(event, pageable);
    }

    public List<Registration> getRegistrationsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return registrationRepository.findByUser(user);
    }

    public Page<Registration> getRegistrationsByUser(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return registrationRepository.findByUser(user, pageable);
    }

    public Optional<Registration> getRegistrationById(UUID id) {
        return registrationRepository.findById(id);
    }

    public Optional<Registration> cancelRegistration(UUID id) {
        Optional<Registration> regOpt = registrationRepository.findById(id);
        if (regOpt.isPresent()) {
            Registration reg = regOpt.get();
            reg.setStatus(RegistrationStatus.CANCELLED);
            Registration savedRegistration = registrationRepository.save(reg);

            // Send cancellation email
            emailService.sendRegistrationCancellation(savedRegistration);

            // Save notification
            saveNotification(reg.getUser(), reg.getEvent(),
                "You have cancelled your registration for event: " + reg.getEvent().getTitle(), "CANCELLATION");

            return Optional.of(savedRegistration);
        }
        return Optional.empty();
    }

    public Optional<Registration> updateRegistration(UUID id, RegistrationUpdateDTO updateDTO) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", id));

        // Update status if provided
        if (updateDTO.getStatus() != null) {
            registration.setStatus(updateDTO.getStatus());
        }

        // Always update note (allows setting to null to clear the note)
        registration.setNote(updateDTO.getNote());

        registration =  registrationRepository.save(registration);

        return Optional.of(registration);
    }

    public boolean isUserRegisteredForEvent(UUID userId, UUID eventId) {
        if (userId == null || eventId == null) {
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        Event event = eventRepository.findById(eventId).orElse(null);

        if (user == null || event == null) {
            return false;
        }

        Optional<Registration> registration = registrationRepository.findByUserAndEvent(user, event);
        return registration.isPresent() && registration.get().getStatus() != RegistrationStatus.CANCELLED;
    }

    public Set<UUID> getRegisteredEventIdsForUser(UUID userId) {
        if (userId == null) {
            return new HashSet<>();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new HashSet<>();
        }

        List<Registration> registrations = registrationRepository.findByUser(user);
        Set<UUID> eventIds = new HashSet<>();

        for (Registration registration : registrations) {
            if (registration.getStatus() != RegistrationStatus.CANCELLED) {
                eventIds.add(registration.getEvent().getId());
            }
        }

        return eventIds;
    }

    private void saveNotification(User user, Event event, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setEvent(event);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }

}


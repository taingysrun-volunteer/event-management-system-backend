package com.taingy.eventmanagementsystem.service;

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
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) return Optional.empty();

        User user = userRepository.findById(userId).orElse(null);
        Registration registration = new Registration();
        registration.setEvent(eventOpt.get());
        registration.setUser(user);
        registration.setStatus("CONFIRMED");
        registration.setCreatedAt(LocalDateTime.now());

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
            reg.setStatus("CANCELLED");
            return Optional.of(registrationRepository.save(reg));
        }
        return Optional.empty();
    }

}


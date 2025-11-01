package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByUser(User user);
    List<Registration> findByEvent(Event event);
    Optional<Registration> findByUserAndEvent(User user, Event event);
}

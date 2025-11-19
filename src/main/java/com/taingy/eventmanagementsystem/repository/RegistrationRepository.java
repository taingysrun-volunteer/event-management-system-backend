package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository extends JpaRepository<Registration, UUID> {
    List<Registration> findByUser(User user);
    Page<Registration> findByUser(User user, Pageable pageable);
    List<Registration> findByEvent(Event event);
    Page<Registration> findByEvent(Event event, Pageable pageable);
    Optional<Registration> findByUserAndEvent(User user, Event event);
    long countByEvent(Event event);
}

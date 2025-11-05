package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByTitleContainingIgnoreCase(String keyword);
    Page<Event> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
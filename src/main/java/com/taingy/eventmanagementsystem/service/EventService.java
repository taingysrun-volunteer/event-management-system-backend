package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.enums.EventStatus;
import com.taingy.eventmanagementsystem.model.Category;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.repository.CategoryRepository;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Page<Event> getAllEvents(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Optional<Event> getEventById(UUID id) {
        return eventRepository.findById(id);
    }

    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }

    public Optional<Event> assignCategories(UUID eventId, Integer categoryId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) return Optional.empty();

        Event event = optionalEvent.get();
        Category category = categoryRepository.findById(categoryId).orElse(null);
        event.setCategory(category);

        return Optional.of(eventRepository.save(event));
    }

    public Page<Event> searchEvents(String keyword, EventStatus status, Integer categoryId, Pageable pageable) {
        if (keyword == null) {
            keyword = "";
        }
        return eventRepository.getEvents(keyword, status, categoryId, pageable);
    }
}


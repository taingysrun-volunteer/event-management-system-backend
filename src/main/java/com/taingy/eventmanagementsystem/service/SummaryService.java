package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.dto.SummaryResponseDTO;
import com.taingy.eventmanagementsystem.repository.CategoryRepository;
import com.taingy.eventmanagementsystem.repository.EventRepository;
import com.taingy.eventmanagementsystem.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public SummaryService(EventRepository eventRepository, UserRepository userRepository,
                         CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public SummaryResponseDTO getSummary() {
        return SummaryResponseDTO.builder()
                .totalEvents(eventRepository.count())
                .totalUsers(userRepository.count())
                .totalCategories(categoryRepository.count())
                .build();
    }
}

package com.taingy.eventmanagementsystem.mapper;

import com.taingy.eventmanagementsystem.dto.EventRequestDTO;
import com.taingy.eventmanagementsystem.dto.EventResponseDTO;
import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.repository.RegistrationRepository;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final RegistrationRepository registrationRepository;

    public EventMapper(UserMapper userMapper, CategoryMapper categoryMapper, RegistrationRepository registrationRepository) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.registrationRepository = registrationRepository;
    }

    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }

        long registeredCount = registrationRepository.countByEvent(event);
        Integer seatsAvailable = event.getCapacity() != null ?
                (int) (event.getCapacity() - registeredCount) : null;

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .price(event.getPrice())
                .capacity(event.getCapacity())
                .availableSeats(seatsAvailable)
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .category(categoryMapper.toResponseDTO(event.getCategory()))
                .organizer(userMapper.toResponseDTO(event.getOrganizer()))
                .registered(null)
                .build();
    }

    public EventResponseDTO toResponseDTO(Event event, boolean isRegistered) {
        if (event == null) {
            return null;
        }

        long registeredCount = registrationRepository.countByEvent(event);
        Integer seatsAvailable = event.getCapacity() != null ?
                (int) (event.getCapacity() - registeredCount) : null;

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .price(event.getPrice())
                .capacity(event.getCapacity())
                .availableSeats(seatsAvailable)
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .category(categoryMapper.toResponseDTO(event.getCategory()))
                .organizer(userMapper.toResponseDTO(event.getOrganizer()))
                .registered(isRegistered)
                .build();
    }

    public Event toEntity(EventRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setPrice(dto.getPrice());
        event.setCapacity(dto.getCapacity());
        event.setStatus(dto.getStatus());
        return event;
    }

    public void updateEntityFromDTO(Event event, EventRequestDTO dto) {
        if (event == null || dto == null) {
            return;
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setLocation(dto.getLocation());
        event.setEventDate(dto.getEventDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setPrice(dto.getPrice());
        event.setCapacity(dto.getCapacity());
        event.setStatus(dto.getStatus());
    }
}

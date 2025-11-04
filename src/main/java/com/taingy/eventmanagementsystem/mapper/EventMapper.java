package com.taingy.eventmanagementsystem.mapper;

import com.taingy.eventmanagementsystem.dto.EventRequestDTO;
import com.taingy.eventmanagementsystem.dto.EventResponseDTO;
import com.taingy.eventmanagementsystem.model.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    public EventMapper(UserMapper userMapper, CategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
    }

    public EventResponseDTO toResponseDTO(Event event) {
        if (event == null) {
            return null;
        }

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
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .category(categoryMapper.toResponseDTO(event.getCategory()))
                .organizer(userMapper.toResponseDTO(event.getOrganizer()))
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

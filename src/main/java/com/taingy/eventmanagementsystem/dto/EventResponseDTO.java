package com.taingy.eventmanagementsystem.dto;

import com.taingy.eventmanagementsystem.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private UUID id;
    private String title;
    private String description;
    private String location;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double price;
    private Integer capacity;
    private EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoryResponseDTO category;
    private UserResponseDTO organizer;
}

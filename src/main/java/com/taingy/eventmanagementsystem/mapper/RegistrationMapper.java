package com.taingy.eventmanagementsystem.mapper;

import com.taingy.eventmanagementsystem.dto.RegistrationResponseDTO;
import com.taingy.eventmanagementsystem.model.Registration;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public RegistrationMapper(EventMapper eventMapper, UserMapper userMapper) {
        this.eventMapper = eventMapper;
        this.userMapper = userMapper;
    }

    public RegistrationResponseDTO toResponseDTO(Registration registration) {
        if (registration == null) {
            return null;
        }

        return RegistrationResponseDTO.builder()
                .id(registration.getId())
                .event(eventMapper.toResponseDTO(registration.getEvent()))
                .user(userMapper.toResponseDTO(registration.getUser()))
                .status(registration.getStatus())
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .build();
    }
}

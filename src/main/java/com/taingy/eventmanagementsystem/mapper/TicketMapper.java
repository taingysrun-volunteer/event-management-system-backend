package com.taingy.eventmanagementsystem.mapper;

import com.taingy.eventmanagementsystem.dto.TicketResponseDTO;
import com.taingy.eventmanagementsystem.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    private final RegistrationMapper registrationMapper;

    public TicketMapper(RegistrationMapper registrationMapper) {
        this.registrationMapper = registrationMapper;
    }

    public TicketResponseDTO toResponseDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .registration(registrationMapper.toResponseDTO(ticket.getRegistration()))
                .ticketNumber(ticket.getTicketNumber())
                .qrCode(ticket.getQrCode())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}

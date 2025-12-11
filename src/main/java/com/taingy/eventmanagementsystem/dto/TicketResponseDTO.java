package com.taingy.eventmanagementsystem.dto;

import com.taingy.eventmanagementsystem.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDTO {
    private UUID id;
    private RegistrationResponseDTO registration;
    private String ticketNumber;
    private String qrCode;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

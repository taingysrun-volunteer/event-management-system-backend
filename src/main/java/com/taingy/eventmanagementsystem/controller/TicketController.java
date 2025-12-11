package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.dto.TicketResponseDTO;
import com.taingy.eventmanagementsystem.exception.BadRequestException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.mapper.TicketMapper;
import com.taingy.eventmanagementsystem.model.Ticket;
import com.taingy.eventmanagementsystem.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final TicketMapper ticketMapper;

    public TicketController(TicketService ticketService, TicketMapper ticketMapper) {
        this.ticketService = ticketService;
        this.ticketMapper = ticketMapper;
    }

    @PostMapping("/generate/{registrationId}")
    public ResponseEntity<TicketResponseDTO> generateTicket(@PathVariable UUID registrationId) {
        Ticket ticket = ticketService.createTicket(registrationId)
                .orElseThrow(() -> new BadRequestException("Unable to generate ticket. Registration may not exist."));
        return ResponseEntity.ok(ticketMapper.toResponseDTO(ticket));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets() {
        List<TicketResponseDTO> tickets = ticketService.getAllTickets().stream()
                .map(ticketMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable UUID id) {
        Ticket ticket = ticketService.getTicketById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return ResponseEntity.ok(ticketMapper.toResponseDTO(ticket));
    }

    @GetMapping("/registration/{registrationId}")
    public ResponseEntity<TicketResponseDTO> getTicketByRegistration(@PathVariable UUID registrationId) {
        Ticket ticket = ticketService.getTicketByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "registrationId", registrationId));
        return ResponseEntity.ok(ticketMapper.toResponseDTO(ticket));
    }

    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<TicketResponseDTO> getTicketByQr(@PathVariable String qrCode) {
        Ticket ticket = ticketService.getTicketByQrCode(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "qrCode", qrCode));
        return ResponseEntity.ok(ticketMapper.toResponseDTO(ticket));
    }

    @PutMapping("/{id}/invalidate")
    public ResponseEntity<TicketResponseDTO> invalidateTicket(@PathVariable UUID id) {
        Ticket ticket = ticketService.invalidateTicket(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return ResponseEntity.ok(ticketMapper.toResponseDTO(ticket));
    }

}

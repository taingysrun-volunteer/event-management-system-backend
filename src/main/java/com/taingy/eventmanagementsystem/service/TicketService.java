package com.taingy.eventmanagementsystem.service;

import com.taingy.eventmanagementsystem.enums.TicketStatus;
import com.taingy.eventmanagementsystem.exception.DuplicateResourceException;
import com.taingy.eventmanagementsystem.exception.ResourceNotFoundException;
import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.Ticket;
import com.taingy.eventmanagementsystem.repository.RegistrationRepository;
import com.taingy.eventmanagementsystem.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final RegistrationRepository registrationRepository;

    public TicketService(TicketRepository ticketRepository, RegistrationRepository registrationRepository) {
        this.ticketRepository = ticketRepository;
        this.registrationRepository = registrationRepository;
    }

    public Optional<Ticket> createTicket(UUID registrationId) {
        // Check if registration exists
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        // Check if ticket already exists for this registration
        Optional<Ticket> existingTicket = ticketRepository.findByRegistration(registration);
        if (existingTicket.isPresent()) {
            throw new DuplicateResourceException("Ticket already exists for this registration");
        }

        // Generate unique QR code
        String qrCode = "QR-" + UUID.randomUUID();

        // Create new ticket
        Ticket ticket = new Ticket();
        ticket.setRegistration(registration);
        ticket.setQrCode(qrCode);
        ticket.setStatus(TicketStatus.VALID);
        // Let @CreationTimestamp and @UpdateTimestamp handle timestamps

        return Optional.of(ticketRepository.save(ticket));
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(UUID id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> getTicketByQrCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode);
    }

    public Optional<Ticket> invalidateTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticket.setStatus(TicketStatus.INVALID);
        // Let @UpdateTimestamp handle timestamp automatically

        return Optional.of(ticketRepository.save(ticket));
    }

}

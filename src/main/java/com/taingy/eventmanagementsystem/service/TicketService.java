package com.taingy.eventmanagementsystem.service;

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
        Optional<Registration> regOpt = registrationRepository.findById(registrationId);
        if (regOpt.isEmpty()) return Optional.empty();

        Registration registration = regOpt.get();

        String qrCode = "QR-" + UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setRegistration(registration);
        ticket.setQrCode(qrCode);
        ticket.setStatus("VALID");
        ticket.setCreatedAt(java.time.LocalDateTime.now());
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

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
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus("INVALID");
            ticket.setUpdatedAt(java.time.LocalDateTime.now());
            return Optional.of(ticketRepository.save(ticket));
        }
        return Optional.empty();
    }

}

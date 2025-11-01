package com.taingy.eventmanagementsystem.controller;

import com.taingy.eventmanagementsystem.model.Ticket;
import com.taingy.eventmanagementsystem.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/generate/{registrationId}")
    public ResponseEntity<Ticket> generateTicket(@PathVariable UUID registrationId) {
        return ticketService.createTicket(registrationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable UUID id) {
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Ticket> getTicketByQr(@PathVariable String qrCode) {
        return ticketService.getTicketByQrCode(qrCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/invalidate")
    public ResponseEntity<Ticket> invalidateTicket(@PathVariable UUID id) {
        return ticketService.invalidateTicket(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}

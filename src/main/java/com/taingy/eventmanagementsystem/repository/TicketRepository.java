package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.Registration;
import com.taingy.eventmanagementsystem.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByQrCode(String qrCode);
    Optional<Ticket> findByRegistration(Registration registration);
}

package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.model.Event;
import com.taingy.eventmanagementsystem.model.Notification;
import com.taingy.eventmanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUser(User user);
    Page<Notification> findByUser(User user, Pageable pageable);
    List<Notification> findByEvent(Event event);
    Page<Notification> findByEvent(Event event, Pageable pageable);
    List<Notification> findByUserAndIsRead(User user, Boolean isRead);
    long countByUserAndIsRead(User user, Boolean isRead);
}

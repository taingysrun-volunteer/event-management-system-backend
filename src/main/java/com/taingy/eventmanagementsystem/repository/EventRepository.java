package com.taingy.eventmanagementsystem.repository;

import com.taingy.eventmanagementsystem.enums.EventStatus;
import com.taingy.eventmanagementsystem.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("""
        SELECT e FROM Event e
        WHERE
            (:keyword IS NULL OR 
                LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:status IS NULL OR e.status = :status)
        AND (:categoryId IS NULL OR e.category.id = :categoryId)
        """)
    Page<Event> getEvents(
            @Param("keyword") String keyword,
            @Param("status") EventStatus status,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );
}
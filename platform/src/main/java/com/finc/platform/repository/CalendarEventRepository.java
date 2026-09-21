package com.finc.platform.repository;

import com.finc.platform.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarEventRepository
        extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByEventDate(LocalDate eventDate);

    List<CalendarEvent> findByEventDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
}
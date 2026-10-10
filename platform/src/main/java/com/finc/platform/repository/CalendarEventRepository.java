package com.finc.platform.repository;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    List<CalendarEvent> findByEventDate(LocalDate eventDate);

    List<CalendarEvent> findByCreatedByUser_id(Long userId);

    List<CalendarEvent> findByEventType(EventType eventType);

    boolean existsByTitleAndEventDate(String title, LocalDate eventDate);

    List<CalendarEvent> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT e FROM CalendarEvent e LEFT JOIN e.createdByUser u WHERE u.id = :userId OR e.visibility = com.finc.platform.entity.EventVisibility.PUBLIC OR e.eventType = com.finc.platform.entity.EventType.HOLIDAY")
    List<CalendarEvent> findAllVisibleToUser(@Param("userId") Long userId);
}
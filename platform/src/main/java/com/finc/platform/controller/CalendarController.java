package com.finc.platform.controller;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.entity.EventType;
import com.finc.platform.repository.CalendarEventRepository;
import com.finc.platform.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarEventService calendarEventService;
    private final CalendarEventRepository calendarEventRepository;

    public CalendarController(CalendarEventService calendarEventService, CalendarEventRepository calendarEventRepository) {
        this.calendarEventService = calendarEventService;
        this.calendarEventRepository = calendarEventRepository;
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEvent>> getAllEvents(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(calendarEventService.getAllEvents(userId));
    }

    @GetMapping("/holidays")
    public ResponseEntity<List<CalendarEvent>> getHolidays() {
        return ResponseEntity.ok(calendarEventRepository.findByEventType(EventType.HOLIDAY));
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEvent> createEvent(
            @RequestBody CalendarEvent event,
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        CalendarEvent created = calendarEventService.createEvent(event, userId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
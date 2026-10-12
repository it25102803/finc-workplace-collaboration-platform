package com.finc.platform.controller;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarEventService calendarEventService;

    public CalendarController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEvent>> getAllEvents(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(calendarEventService.getAllEvents(userId));
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEvent> createEvent(
            @RequestBody CalendarEvent event,
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        return ResponseEntity.ok(calendarEventService.createEvent(event, userId));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
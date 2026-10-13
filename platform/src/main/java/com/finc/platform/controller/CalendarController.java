package com.finc.platform.controller;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.service.CalendarEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// actorId is the logged-in user, filled in from the login token by RoleGuardFilter.
@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarEventService calendarEventService;

    public CalendarController(CalendarEventService calendarEventService) {
        this.calendarEventService = calendarEventService;
    }

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEvent>> getAllEvents(@RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(calendarEventService.getAllEvents(actorId));
    }

    // attendeeIds: users invited to a meeting (they see it even when it is private).
    @PostMapping("/events")
    public ResponseEntity<CalendarEvent> createEvent(
            @RequestBody CalendarEvent event,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) List<Long> attendeeIds
    ) {
        return ResponseEntity.ok(calendarEventService.createEvent(event, actorId, attendeeIds));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id,
                                            @RequestParam(required = false) Long actorId) {
        calendarEventService.deleteEvent(id, actorId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
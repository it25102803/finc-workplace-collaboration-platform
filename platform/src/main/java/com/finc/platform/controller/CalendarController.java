package com.finc.platform.controller;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*")
public class CalendarController {

    private final CalendarEventService calendarEventService;

    public CalendarController(
            CalendarEventService calendarEventService
    ) {
        this.calendarEventService = calendarEventService;
    }

    // GET ALL EVENTS
    @GetMapping
    public ResponseEntity<List<CalendarEvent>> getAllEvents() {

        return ResponseEntity.ok(
                calendarEventService.getAllEvents()
        );
    }

    // GET EVENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                calendarEventService.getEventById(id)
        );
    }

    // CREATE EVENT
    @PostMapping
    public ResponseEntity<CalendarEvent> createEvent(
            @RequestBody CalendarEvent event,
            @RequestParam(required = false)
            Long createdByUserId
    ) {

        return ResponseEntity.ok(
                calendarEventService.createEvent(
                        event,
                        createdByUserId
                )
        );
    }

    // UPDATE EVENT
    @PutMapping("/{id}")
    public ResponseEntity<CalendarEvent> updateEvent(
            @PathVariable Long id,
            @RequestBody CalendarEvent event
    ) {

        return ResponseEntity.ok(
                calendarEventService.updateEvent(
                        id,
                        event
                )
        );
    }

    // DELETE EVENT
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(
            @PathVariable Long id
    ) {

        calendarEventService.deleteEvent(id);

        return ResponseEntity.ok(
                "Calendar event deleted successfully"
        );
    }

    // EVENTS FOR SPECIFIC DATE
    @GetMapping("/date/{date}")
    public ResponseEntity<List<CalendarEvent>> getEventsByDate(
            @PathVariable String date
    ) {

        LocalDate localDate = LocalDate.parse(date);

        return ResponseEntity.ok(
                calendarEventService.getEventsByDate(
                        localDate
                )
        );
    }

    // EVENTS BETWEEN TWO DATES
    @GetMapping("/range")
    public ResponseEntity<List<CalendarEvent>> getEventsBetween(
            @RequestParam String start,
            @RequestParam String end
    ) {

        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        return ResponseEntity.ok(
                calendarEventService.getEventsBetween(
                        startDate,
                        endDate
                )
        );
    }
}
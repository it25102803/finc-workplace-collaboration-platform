package com.finc.platform.service;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.entity.User;
import com.finc.platform.repository.CalendarEventRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarEventService {

    private final CalendarEventRepository eventRepository;
    private final UserRepository userRepository;

    public CalendarEventService(
            CalendarEventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<CalendarEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    public CalendarEvent getEventById(Long id) {

        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Calendar event not found"
                        ));
    }

    public CalendarEvent createEvent(
            CalendarEvent event,
            Long createdByUserId
    ) {

        if (createdByUserId != null) {

            User user = userRepository.findById(createdByUserId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            ));

            event.setCreatedBy(user);
        }

        return eventRepository.save(event);
    }

    public CalendarEvent updateEvent(
            Long id,
            CalendarEvent updatedEvent
    ) {

        CalendarEvent existing = getEventById(id);

        existing.setTitle(updatedEvent.getTitle());
        existing.setDescription(updatedEvent.getDescription());
        existing.setEventDate(updatedEvent.getEventDate());
        existing.setStartTime(updatedEvent.getStartTime());
        existing.setEndTime(updatedEvent.getEndTime());
        existing.setEventType(updatedEvent.getEventType());

        return eventRepository.save(existing);
    }

    public void deleteEvent(Long id) {

        if (!eventRepository.existsById(id)) {
            throw new RuntimeException(
                    "Calendar event not found"
            );
        }

        eventRepository.deleteById(id);
    }

    public List<CalendarEvent> getEventsByDate(
            LocalDate date
    ) {
        return eventRepository.findByEventDate(date);
    }

    public List<CalendarEvent> getEventsBetween(
            LocalDate start,
            LocalDate end
    ) {
        return eventRepository.findByEventDateBetween(
                start,
                end
        );
    }
}
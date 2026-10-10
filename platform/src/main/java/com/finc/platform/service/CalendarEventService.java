package com.finc.platform.service;

import com.finc.platform.entity.CalendarEvent;
import com.finc.platform.entity.EventType;
import com.finc.platform.entity.User;
import com.finc.platform.repository.CalendarEventRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository, UserRepository userRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.userRepository = userRepository;
    }

    public List<CalendarEvent> getAllEvents(Long userId) {
        if (userId != null) {
            return calendarEventRepository.findAllVisibleToUser(userId);
        }
        return calendarEventRepository.findAll();
    }

    public List<CalendarEvent> getEventsByDate(LocalDate date) {
        return calendarEventRepository.findByEventDate(date);
    }

    public List<CalendarEvent> getEventsByUser(Long userId) {
        return calendarEventRepository.findByCreatedByUser_id(userId);
    }

    @Transactional
    public CalendarEvent createEvent(CalendarEvent event, Long userId) {
        if (userId != null) {
            User creator = userRepository.findById(userId).orElse(null);
            event.setCreatedByUser(creator);
        } else if (event.getCreatedByUser() == null) {
            event.setCreatedByUser(userRepository.findAll().stream().findFirst().orElse(null));
        }

        if (event.getEventDate() != null) {
            if (event.getStartTime() == null) event.setStartTime(event.getEventDate().atStartOfDay());
            if (event.getEndTime() == null) event.setEndTime(event.getEventDate().atTime(LocalTime.MAX));
        }

        return calendarEventRepository.save(event);
    }

    public void deleteEvent(Long eventId) {
        calendarEventRepository.deleteById(eventId);
    }
}
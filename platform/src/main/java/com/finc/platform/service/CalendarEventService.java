package com.finc.platform.service;

import com.finc.platform.entity.*;
import com.finc.platform.repository.CalendarEventRepository;
import com.finc.platform.repository.TaskRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CalendarEventService(
            CalendarEventRepository calendarEventRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.calendarEventRepository = calendarEventRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<CalendarEvent> getAllEvents(Long userId) {
        // 1. Ensure 2026 Sri Lankan holidays exist in MySQL
        ensureHolidaysSeeded();

        // 2. Ensure all tasks with due dates are synced into calendar_events
        syncExistingTaskDeadlines();

        // 3. Return all events for display
        return calendarEventRepository.findAll();
    }

    @Transactional
    public CalendarEvent createEvent(CalendarEvent event, Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(event::setCreatedByUser);
        }
        if (event.getEventDate() == null && event.getStartTime() != null) {
            event.setEventDate(event.getStartTime().toLocalDate());
        }
        return calendarEventRepository.save(event);
    }

    public void deleteEvent(Long eventId) {
        calendarEventRepository.deleteById(eventId);
    }

    private void syncExistingTaskDeadlines() {
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            if (task.getDueDate() != null) {
                String deadlineTitle = "📌 " + task.getTitle();
                boolean exists = calendarEventRepository.findAll().stream().anyMatch(e ->
                        (e.getRelatedTask() != null && e.getRelatedTask().getTaskId().equals(task.getTaskId()))
                                || (e.getTitle().equals(deadlineTitle) && task.getDueDate().equals(e.getEventDate()))
                );

                if (!exists) {
                    CalendarEvent deadline = new CalendarEvent();
                    deadline.setTitle(deadlineTitle);
                    deadline.setDescription("Task deadline: " + task.getTitle() + " (Priority: " + task.getPriority() + ")");
                    deadline.setEventType(EventType.DEADLINE);
                    deadline.setEventDate(task.getDueDate());
                    deadline.setStartTime(task.getDueDate().atStartOfDay());
                    deadline.setEndTime(task.getDueDate().atTime(LocalTime.MAX));
                    deadline.setAllDay(true);
                    deadline.setVisibility(EventVisibility.PUBLIC);
                    deadline.setRelatedTask(task);
                    deadline.setCreatedByUser(task.getCreator());
                    calendarEventRepository.save(deadline);
                }
            }
        }
    }

    private void ensureHolidaysSeeded() {
        if (calendarEventRepository.findByEventType(EventType.HOLIDAY).isEmpty()) {
            Map<LocalDate, String> holidays = new LinkedHashMap<>();
            holidays.put(LocalDate.of(2026, 1, 3), "🌕 Duruthu Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 1, 15), "🌾 Tamil Thai Pongal Day");
            holidays.put(LocalDate.of(2026, 2, 1), "🌕 Navam Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 2, 4), "🇱🇰 National Independence Day");
            holidays.put(LocalDate.of(2026, 2, 16), "🕉️ Mahasivarathri Day");
            holidays.put(LocalDate.of(2026, 3, 3), "🌕 Medin Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 3, 20), "🌙 Id Ul-Fitr (Ramazan)");
            holidays.put(LocalDate.of(2026, 4, 1), "🌕 Bak Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 4, 3), "✝️ Good Friday");
            holidays.put(LocalDate.of(2026, 4, 13), "☀️ Sinhala & Tamil New Year Eve");
            holidays.put(LocalDate.of(2026, 4, 14), "☀️ Sinhala & Tamil New Year Day");
            holidays.put(LocalDate.of(2026, 5, 1), "🛠️ May Day (Workers' Day)");
            holidays.put(LocalDate.of(2026, 5, 27), "🌙 Id Ul-Alha (Hadji)");
            holidays.put(LocalDate.of(2026, 5, 30), "🌕 Vesak Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 5, 31), "🌕 Day after Vesak Poya");
            holidays.put(LocalDate.of(2026, 6, 29), "🌕 Poson Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 7, 28), "🌕 Esala Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 8, 26), "🕌 Milad-Un-Nabi");
            holidays.put(LocalDate.of(2026, 8, 27), "🌕 Nikini Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 9, 25), "🌕 Binara Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 10, 25), "🌕 Vap Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 11, 8), "🪔 Deepavali Festival Day");
            holidays.put(LocalDate.of(2026, 11, 23), "🌕 Il Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 12, 23), "🌕 Unduvap Full Moon Poya Day");
            holidays.put(LocalDate.of(2026, 12, 25), "🎄 Christmas Day");

            for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {
                CalendarEvent holiday = new CalendarEvent();
                holiday.setTitle(entry.getValue());
                holiday.setDescription("Official National Holiday in Sri Lanka");
                holiday.setEventType(EventType.HOLIDAY);
                holiday.setEventDate(entry.getKey());
                holiday.setStartTime(entry.getKey().atStartOfDay());
                holiday.setEndTime(entry.getKey().atTime(LocalTime.MAX));
                holiday.setAllDay(true);
                holiday.setVisibility(EventVisibility.PUBLIC);
                calendarEventRepository.save(holiday);
            }
        }
    }
}
package com.finc.platform.service;

import com.finc.platform.entity.*;
import com.finc.platform.repository.CalendarEventRepository;
import com.finc.platform.repository.TaskRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

// Holidays are added once at startup by SriLankaHolidaySeeder; task deadlines are kept in step by TaskService.
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

    // Events this user may see: holidays, deadlines and public events, their department's events,
    // and private/shared events they created or were invited to.
    @Transactional
    public List<CalendarEvent> getAllEvents(Long userId) {
        syncMissingTaskDeadlines();
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        return calendarEventRepository.findAll().stream()
                .filter(event -> canView(event, user))
                .toList();
    }

    public boolean canView(CalendarEvent event, User user) {
        if (event.getEventType() == EventType.HOLIDAY || event.getEventType() == EventType.DEADLINE) {
            return true;
        }
        if (isCreator(event, user) || isAttendee(event, user)) {
            return true;
        }
        EventVisibility visibility = event.getVisibility() == null ? EventVisibility.PUBLIC : event.getVisibility();
        if (visibility == EventVisibility.PUBLIC) {
            return true;
        }
        if (visibility == EventVisibility.DEPARTMENT) {
            Long mine = user.getDepartment() == null ? null : user.getDepartment().getDepartmentId();
            User creator = event.getCreatedByUser();
            Long theirs = creator == null || creator.getDepartment() == null ? null : creator.getDepartment().getDepartmentId();
            return mine != null && mine.equals(theirs);
        }
        return false;
    }

    @Transactional
    public CalendarEvent createEvent(CalendarEvent event, Long userId, List<Long> attendeeIds) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (event.getEventType() == EventType.HOLIDAY || event.getEventType() == EventType.DEADLINE) {
            throw new IllegalArgumentException("Holidays and task deadlines are added automatically");
        }
        if (userId != null) {
            userRepository.findById(userId).ifPresent(event::setCreatedByUser);
        }
        if (event.getEventDate() == null && event.getStartTime() != null) {
            event.setEventDate(event.getStartTime().toLocalDate());
        }
        if (event.getVisibility() == null) {
            event.setVisibility(EventVisibility.PUBLIC);
        }
        Set<User> attendees = new HashSet<>();
        if (attendeeIds != null && !attendeeIds.isEmpty()) {
            attendees.addAll(userRepository.findAllById(attendeeIds));
        }
        event.setAttendees(attendees);
        return calendarEventRepository.save(event);
    }

    // The creator can delete their event; a system administrator can delete any event.
    // Deadlines go away with their task, so they are not deleted here.
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new SecurityException("Login required");
        }
        if (event.getEventType() == EventType.DEADLINE) {
            throw new SecurityException("Delete or change the task to remove its deadline");
        }
        boolean systemAdmin = user.getRole() != null && user.getRole().getRoleType() == RoleType.ROLE_SYSTEM_ADMIN;
        if (!systemAdmin && !isCreator(event, user)) {
            throw new SecurityException("Only the person who created this event can delete it");
        }
        calendarEventRepository.delete(event);
    }

    private boolean isCreator(CalendarEvent event, User user) {
        return event.getCreatedByUser() != null && Objects.equals(event.getCreatedByUser().getId(), user.getId());
    }

    private boolean isAttendee(CalendarEvent event, User user) {
        return event.getAttendees() != null && event.getAttendees().stream()
                .anyMatch(attendee -> Objects.equals(attendee.getId(), user.getId()));
    }

    // Older tasks created before deadlines were synced: add their deadline event once.
    private void syncMissingTaskDeadlines() {
        Set<Long> tasksWithDeadline = calendarEventRepository.findByEventType(EventType.DEADLINE).stream()
                .filter(event -> event.getRelatedTask() != null)
                .map(event -> event.getRelatedTask().getTaskId())
                .collect(Collectors.toSet());

        for (Task task : taskRepository.findAll()) {
            if (task.getDueDate() == null || tasksWithDeadline.contains(task.getTaskId())) {
                continue;
            }
            CalendarEvent deadline = new CalendarEvent();
            deadline.setTitle("📌 Deadline: " + task.getTitle());
            deadline.setDescription("Task deadline: " + task.getTitle() + " (Priority: " + task.getPriority() + ")");
            deadline.setEventType(EventType.DEADLINE);
            deadline.setEventDate(task.getDueDate());
            deadline.setStartTime(task.getDueDate().atStartOfDay());
            deadline.setEndTime(task.getDueDate().atTime(LocalTime.MAX));
            deadline.setAllDay(true);
            deadline.setVisibility(EventVisibility.PUBLIC);
            deadline.setRelatedTask(task);
            deadline.setCreatedByUser(task.getCreator());
            task.setRelatedEvent(calendarEventRepository.save(deadline));
        }
    }
}
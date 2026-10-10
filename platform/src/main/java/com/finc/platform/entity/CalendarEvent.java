package com.finc.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType = EventType.MEETING;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private LocalDate eventDate;
    private String location;
    private boolean isAllDay = false;
    private boolean isPrivate = false;

    @Enumerated(EnumType.STRING)
    private EventVisibility visibility = EventVisibility.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "roles", "department", "hibernateLazyInitializer", "handler"})
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonIgnore // Break circular serialization
    private Task relatedTask;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_attendees",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"password", "roles", "department", "hibernateLazyInitializer", "handler"})
    private Set<User> attendees = new HashSet<>();

    public CalendarEvent() {}

    public User getOrganizer() { return this.createdByUser; }
    public void setOrganizer(User organizer) { this.createdByUser = organizer; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (startTime != null && this.eventDate == null) {
            this.eventDate = startTime.toLocalDate();
        }
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDate getEventDate() {
        if (this.eventDate != null) return this.eventDate;
        return this.startTime != null ? this.startTime.toLocalDate() : null;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
        if (this.startTime == null && eventDate != null) this.startTime = eventDate.atTime(LocalTime.MIN);
        if (this.endTime == null && eventDate != null) this.endTime = eventDate.atTime(LocalTime.MAX);
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isAllDay() { return isAllDay; }
    public void setAllDay(boolean allDay) { isAllDay = allDay; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public EventVisibility getVisibility() { return visibility; }
    public void setVisibility(EventVisibility visibility) {
        this.visibility = visibility;
        if (visibility != null) this.isPrivate = (visibility == EventVisibility.PRIVATE);
    }

    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }

    public Task getRelatedTask() { return relatedTask; }
    public void setRelatedTask(Task relatedTask) { this.relatedTask = relatedTask; }

    public Set<User> getAttendees() { return attendees; }
    public void setAttendees(Set<User> attendees) { this.attendees = attendees; }
}
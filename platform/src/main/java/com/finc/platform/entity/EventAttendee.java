package com.finc.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "event_attendees")
public class EventAttendee {

    @EmbeddedId
    private EventAttendeeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "response", nullable = false)
    private AttendanceResponse response = AttendanceResponse.PENDING;

    public EventAttendee() {
    }

    public EventAttendeeId getId() {
        return id;
    }

    public void setId(EventAttendeeId id) {
        this.id = id;
    }

    public CalendarEvent getEvent() {
        return event;
    }

    public void setEvent(CalendarEvent event) {
        this.event = event;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AttendanceResponse getResponse() {
        return response;
    }

    public void setResponse(AttendanceResponse response) {
        this.response = response;
    }
}
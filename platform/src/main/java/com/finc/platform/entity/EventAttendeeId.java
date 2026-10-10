package com.finc.platform.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EventAttendeeId implements Serializable {

    private Long eventId;

    private Long userId;

    public EventAttendeeId() {
    }

    public EventAttendeeId(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof EventAttendeeId)) {
            return false;
        }

        EventAttendeeId that = (EventAttendeeId) o;

        return Objects.equals(eventId, that.eventId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, userId);
    }
}
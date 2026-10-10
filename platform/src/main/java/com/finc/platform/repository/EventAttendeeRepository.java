package com.finc.platform.repository;

import com.finc.platform.entity.EventAttendee;
import com.finc.platform.entity.EventAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventAttendeeRepository
        extends JpaRepository<EventAttendee, EventAttendeeId> {

    List<EventAttendee> findByIdEventId(Long eventId);

    List<EventAttendee> findByIdUserId(Long userId);

    Optional<EventAttendee> findByIdEventIdAndIdUserId(
            Long eventId,
            Long userId
    );

    void deleteByIdEventIdAndIdUserId(
            Long eventId,
            Long userId
    );
}
package com.finc.platform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PresenceScheduler {

    @Autowired
    private PresenceService presenceService;

    /** Every 2 minutes: 5 min idle -> AWAY, 15 min idle -> OFFLINE. */
    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void sweep() {
        int changed = presenceService.sweepStaleUsers(5, 15);
        if (changed > 0) {
            System.out.println("[Presence] " + changed + " user(s) status updated");
        }
    }
}
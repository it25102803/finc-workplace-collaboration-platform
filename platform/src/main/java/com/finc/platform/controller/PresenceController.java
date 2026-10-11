package com.finc.platform.controller;

import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.service.PresenceService;
import com.finc.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@CrossOrigin(origins = "*")
public class PresenceController {

    @Autowired private PresenceService presenceService;
    @Autowired private UserService userService;

    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestParam Long userId) {
        presenceService.heartbeat(userId);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/offline")
    public ResponseEntity<?> goOffline(@RequestParam Long userId) {
        try {
            userService.setStatus(userId, UserStatus.OFFLINE);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/status")
    public ResponseEntity<?> setStatus(@RequestParam Long userId,
                                       @RequestParam String status) {
        try {
            UserStatus s = UserStatus.valueOf(status.toUpperCase());
            User u = userService.setStatus(userId, s);
            return ResponseEntity.ok(u);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }

    @PostMapping("/sweep")
    public ResponseEntity<?> sweep(@RequestParam(defaultValue = "5") int awayAfter,
                                   @RequestParam(defaultValue = "15") int offlineAfter) {
        int changed = presenceService.sweepStaleUsers(awayAfter, offlineAfter);
        return ResponseEntity.ok(Map.of("changed", changed));
    }
}
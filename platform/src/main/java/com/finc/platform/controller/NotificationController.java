package com.finc.platform.controller;

import com.finc.platform.entity.Notification;
import com.finc.platform.entity.User;
import com.finc.platform.repository.NotificationRepository;
import com.finc.platform.service.ChatAccess;
import com.finc.platform.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Users only ever see and update their own notifications (the logged-in user's email).
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final ChatAccess chatAccess;

    public NotificationController(NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  ChatAccess chatAccess) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.chatAccess = chatAccess;
    }

    @GetMapping
    public List<Notification> getNotifications(@RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        return notificationService.getForUser(actor.getEmail());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id,
                                                 @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
            if (notification.getRecipientEmail() == null
                    || !notification.getRecipientEmail().equalsIgnoreCase(actor.getEmail())) {
                return ResponseEntity.status(403).build();
            }
            return ResponseEntity.ok(notificationService.markRead(id));
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}

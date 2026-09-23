package com.example.workplace.controller;

import com.example.workplace.model.Message;
import com.example.workplace.service.CommunicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessageController {

    private final CommunicationService communicationService;

    public MessageController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping("/messages")
    public List<Message> getAllMessages() {
        return communicationService.getAllChannels().stream()
                .flatMap(channel -> communicationService.getMessagesForChannel(channel.getId()).stream())
                .toList();
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        try {
            if (message == null || message.getChannel() == null || message.getChannel().getId() == null) {
                return ResponseEntity.badRequest().build();
            }

            Message saved = communicationService.createMessage(message.getChannel().getId(), message);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            @RequestParam String sender) {
        try {
            boolean deleted = communicationService.deleteMessage(id, sender);
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content on success
            }
            return ResponseEntity.status(403).build(); // 403 Forbidden if sender doesn't match or message not found
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build(); // 404 Not Found if ID is invalid
        }
    }
}

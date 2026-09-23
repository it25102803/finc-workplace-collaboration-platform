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
}

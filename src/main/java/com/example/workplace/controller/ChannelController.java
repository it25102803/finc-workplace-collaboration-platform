package com.example.workplace.controller;

import com.example.workplace.model.Channel;
import com.example.workplace.model.Message;
import com.example.workplace.service.CommunicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    private final CommunicationService communicationService;

    public ChannelController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping
    public List<Channel> getAllChannels() {
        return communicationService.getAllChannels();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannelById(@PathVariable Long id) {
        return communicationService.getChannelById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Channel> createChannel(@RequestBody Channel channel) {
        try {
            return ResponseEntity.ok(communicationService.createChannel(channel));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id) {
        return communicationService.getMessagesForChannel(id);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> createMessage(
            @PathVariable Long id,
            @RequestBody Message message) {

        try {
            Message saved = communicationService.createMessage(id, message);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/{id}/messages/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Message> createAttachmentMessage(
            @PathVariable Long id,
            @RequestParam String sender,
            @RequestParam(required = false, defaultValue = "") String content,
            @RequestPart MultipartFile attachment) {

        try {
            return ResponseEntity.ok(communicationService.createAttachmentMessage(id, sender, content, attachment));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/members")
    public ResponseEntity<Channel> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return communicationService.getChannelById(id)
                .map(channel -> {
                    channel.addMember(email);
                    return ResponseEntity.ok(communicationService.createChannel(channel));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<Channel> removeMember(
            @PathVariable Long id,
            @RequestParam String email) {

        if (email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return communicationService.getChannelById(id)
                .map(channel -> {
                    channel.removeMember(email);
                    return ResponseEntity.ok(communicationService.createChannel(channel));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

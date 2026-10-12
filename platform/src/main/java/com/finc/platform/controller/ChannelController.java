package com.finc.platform.controller;

import com.finc.platform.entity.Channel;
import com.finc.platform.entity.ChannelType;
import com.finc.platform.entity.Message;
import com.finc.platform.entity.User;
import com.finc.platform.service.ChatAccess;
import com.finc.platform.service.CommunicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    private final CommunicationService communicationService;
    private final ChatAccess chatAccess;

    public ChannelController(CommunicationService communicationService, ChatAccess chatAccess) {
        this.communicationService = communicationService;
        this.chatAccess = chatAccess;
    }

    // Only the channels this user may see: public channels plus their own group chats and direct messages.
    @GetMapping
    public List<Channel> getAllChannels(@RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        return communicationService.getAllChannels().stream()
                .filter(channel -> chatAccess.canView(channel, actor))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Channel> getChannelById(@PathVariable Long id,
                                                  @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        Channel channel = findOrThrow(id);
        chatAccess.requireView(channel, actor);
        return ResponseEntity.ok(channel);
    }

    @PostMapping
    public ResponseEntity<Channel> createChannel(@RequestBody Channel channel,
                                                 @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        ChannelType type = channel.getType() == null ? ChannelType.PUBLIC : channel.getType();
        channel.setType(type);
        if (type == ChannelType.PUBLIC && !chatAccess.isAdmin(actor)) {
            throw new SecurityException("Only department or system administrators can create public channels");
        }

        List<String> members = new ArrayList<>(channel.getMemberEmails() == null ? List.of() : channel.getMemberEmails());
        boolean includesActor = members.stream().anyMatch(email -> email != null && email.equalsIgnoreCase(actor.getEmail()));
        if (!includesActor) {
            members.add(actor.getEmail());
        }
        if (type == ChannelType.DIRECT && members.size() != 2) {
            throw new IllegalArgumentException("A direct message needs exactly one other person");
        }
        channel.setMemberEmails(members);
        return ResponseEntity.ok(communicationService.createChannel(channel));
    }

    // Rename a group chat (members) or a public channel (admins).
    @PutMapping("/{id}/name")
    public ResponseEntity<Channel> renameChannel(@PathVariable Long id,
                                                 @RequestBody Map<String, String> request,
                                                 @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        Channel channel = findOrThrow(id);
        chatAccess.requireManage(channel, actor);
        String name = request.get("name");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        channel.setName(name.trim());
        return ResponseEntity.ok(communicationService.createChannel(channel));
    }

    @GetMapping("/{id}/messages")
    public List<Message> getMessages(@PathVariable Long id,
                                     @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        chatAccess.requireView(findOrThrow(id), actor);
        return communicationService.getMessagesForChannel(id);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Message> createMessage(
            @PathVariable Long id,
            @RequestBody Message message,
            @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        chatAccess.requireView(findOrThrow(id), actor);
        message.setSender(chatAccess.senderName(actor));
        return ResponseEntity.ok(communicationService.createMessage(id, message));
    }

    @PostMapping(value = "/{id}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Message> createMultipartMessage(
            @PathVariable Long id,
            @RequestParam(name = "content", required = false, defaultValue = "") String content,
            @RequestPart(name = "attachment", required = false) MultipartFile attachment,
            @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        chatAccess.requireView(findOrThrow(id), actor);
        String sender = chatAccess.senderName(actor);
        if (attachment != null && !attachment.isEmpty()) {
            return ResponseEntity.ok(communicationService.createAttachmentMessage(id, sender, content, attachment));
        }
        Message message = new Message();
        message.setSender(sender);
        message.setContent(content);
        return ResponseEntity.ok(communicationService.createMessage(id, message));
    }

    @PostMapping(value = "/{id}/messages/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Message> createAttachmentMessage(
            @PathVariable Long id,
            @RequestParam(name = "content", required = false, defaultValue = "") String content,
            @RequestPart("attachment") MultipartFile attachment,
            @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        chatAccess.requireView(findOrThrow(id), actor);
        return ResponseEntity.ok(communicationService.createAttachmentMessage(id, chatAccess.senderName(actor), content, attachment));
    }

    @PutMapping("/{id}/members")
    public ResponseEntity<Channel> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Channel channel = findOrThrow(id);
        chatAccess.requireManage(channel, actor);
        channel.addMember(email);
        return ResponseEntity.ok(communicationService.createChannel(channel));
    }

    @DeleteMapping("/{id}/members")
    public ResponseEntity<Channel> removeMember(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        if (email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Channel channel = findOrThrow(id);
        chatAccess.requireManage(channel, actor);
        channel.removeMember(email);
        return ResponseEntity.ok(communicationService.createChannel(channel));
    }

    private Channel findOrThrow(Long id) {
        return communicationService.getChannelById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Channel not found"));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> notFound(java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

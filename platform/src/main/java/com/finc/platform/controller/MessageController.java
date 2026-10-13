package com.finc.platform.controller;

import com.finc.platform.entity.Channel;
import com.finc.platform.entity.Message;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.dto.MessageSearchResult;
import com.finc.platform.repository.MessageRepository;
import com.finc.platform.service.ChatAccess;
import com.finc.platform.service.CommunicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// The sender is always the logged-in user (actorId from the login token); the old ?sender= and
// ?memberEmail= parameters are still accepted from the page but ignored.
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessageController {

    private final CommunicationService communicationService;
    private final MessageRepository messageRepository;
    private final ChatAccess chatAccess;

    public MessageController(CommunicationService communicationService,
                             MessageRepository messageRepository,
                             ChatAccess chatAccess) {
        this.communicationService = communicationService;
        this.messageRepository = messageRepository;
        this.chatAccess = chatAccess;
    }

    @GetMapping("/messages")
    public List<Message> getAllMessages(@RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        return communicationService.getAllChannels().stream()
                .filter(channel -> chatAccess.canView(channel, actor))
                .flatMap(channel -> communicationService.getMessagesForChannel(channel.getId()).stream())
                .toList();
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> createMessage(@RequestBody Message message,
                                                 @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            if (message == null || message.getChannel() == null || message.getChannel().getId() == null) {
                return ResponseEntity.badRequest().build();
            }
            Channel channel = communicationService.getChannelById(message.getChannel().getId()).orElse(null);
            if (channel == null) {
                return ResponseEntity.badRequest().build();
            }
            chatAccess.requireView(channel, actor);
            message.setSender(chatAccess.senderName(actor));

            Message saved = communicationService.createMessage(channel.getId(), message);
            return ResponseEntity.ok(saved);
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Only results from conversations the user can open.
    @GetMapping("/messages/search")
    public List<MessageSearchResult> searchMessages(@RequestParam("q") String query,
                                                    @RequestParam(required = false) Long actorId) {
        User actor = chatAccess.actor(actorId);
        Set<Long> visible = communicationService.getAllChannels().stream()
                .filter(channel -> chatAccess.canView(channel, actor))
                .map(Channel::getId)
                .collect(Collectors.toSet());
        return communicationService.searchMessages(query).stream()
                .filter(result -> visible.contains(result.channelId()))
                .toList();
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<Message> editMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            return ResponseEntity.ok(communicationService.editMessage(id, chatAccess.senderName(actor), request.get("content")));
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/messages/{id}/reactions")
    public ResponseEntity<Message> toggleReaction(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            Message message = messageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found"));
            chatAccess.requireView(message.getChannel(), actor);
            return ResponseEntity.ok(communicationService.toggleReaction(id, chatAccess.senderName(actor), request.get("emoji")));
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/messages/{id}/pin")
    public ResponseEntity<Message> togglePin(
            @PathVariable Long id,
            @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            Message message = messageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found"));
            Channel channel = message.getChannel();
            chatAccess.requireView(channel, actor);
            // Anyone who can see a public channel may pin in it; the service checks membership.
            if (!chatAccess.isMember(channel, actor)) {
                channel.addMember(actor.getEmail());
                communicationService.createChannel(channel);
            }
            return ResponseEntity.ok(communicationService.togglePin(id, actor.getEmail()));
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    // The sender can delete their own message; a system administrator can remove any message.
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id,
            @RequestParam(required = false) Long actorId) {
        try {
            User actor = chatAccess.actor(actorId);
            Message message = messageRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Message not found"));
            boolean systemAdmin = actor.getRole() != null
                    && actor.getRole().getRoleType() == RoleType.ROLE_SYSTEM_ADMIN;
            String requester = systemAdmin ? message.getSender() : chatAccess.senderName(actor);
            boolean deleted = communicationService.deleteMessage(id, requester);
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content on success
            }
            return ResponseEntity.status(403).build(); // 403 Forbidden if it is someone else's message
        } catch (SecurityException ex) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build(); // 404 Not Found if ID is invalid
        }
    }
}

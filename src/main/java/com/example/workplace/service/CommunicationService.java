package com.example.workplace.service;

import com.example.workplace.model.Channel;
import com.example.workplace.model.ChannelType;
import com.example.workplace.model.KnowledgeItem;
import com.example.workplace.model.Message;
import com.example.workplace.repository.ChannelRepository;
import com.example.workplace.repository.KnowledgeRepository;
import com.example.workplace.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class CommunicationService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeRepository knowledgeRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public CommunicationService(ChannelRepository channelRepository,
                                MessageRepository messageRepository,
                                KnowledgeRepository knowledgeRepository,
                                NotificationService notificationService,
                                SimpMessagingTemplate messagingTemplate) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.knowledgeRepository = knowledgeRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    public Optional<Channel> getChannelById(Long id) {
        return channelRepository.findById(id);
    }

    @Transactional
    public Channel createChannel(Channel channel) {
        if (channel == null || channel.getName() == null || channel.getName().isBlank()) {
            throw new IllegalArgumentException("Channel name is required");
        }

        if (channel.getType() == null) {
            channel.setType(ChannelType.PUBLIC);
        }

        if (channel.getMemberEmails() == null) {
            channel.setMemberEmails(new java.util.ArrayList<>());
        }

        return channelRepository.save(channel);
    }

    public List<Message> getMessagesForChannel(Long channelId) {
        return messageRepository.findByChannelIdOrderByTimestampAsc(channelId);
    }

    @Transactional
    public Message createMessage(Long channelId, Message message) {
        if (message == null || ((message.getContent() == null || message.getContent().isBlank())
                && (message.getAttachmentData() == null || message.getAttachmentData().isBlank()))) {
            throw new IllegalArgumentException("Message content or attachment is required");
        }

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        message.setChannel(channel);
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        Message saved = messageRepository.save(message);
        List<String> recipients = new java.util.ArrayList<>(channel.getMemberEmails());
        String mention = recipients.stream()
            .filter(recipient -> message.getContent() != null
                && message.getContent().toLowerCase().contains("@" + recipient.substring(0, recipient.indexOf('@')).toLowerCase()))
            .findFirst()
            .orElse(null);
        notificationService.notifyMembers(saved, recipients, mention);
        messagingTemplate.convertAndSend("/topic/channels/" + channelId, saved);
        return saved;
    }

    @Transactional
    public Message createAttachmentMessage(Long channelId, String sender, String content,
                                            MultipartFile attachment) {
        if (attachment == null || attachment.isEmpty()) {
            throw new IllegalArgumentException("Attachment is required");
        }

        if (attachment.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Attachment must be 10 MB or smaller");
        }

        try {
            Message message = new Message();
            message.setSender(sender == null || sender.isBlank() ? "Unknown user" : sender);
            message.setContent(content == null ? "" : content.trim());
            message.setAttachmentName(attachment.getOriginalFilename());
            message.setAttachmentType(attachment.getContentType() == null
                    ? "application/octet-stream" : attachment.getContentType());
            message.setAttachmentData(Base64.getEncoder().encodeToString(attachment.getBytes()));
            return createMessage(channelId, message);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read attachment", ex);
        }
    }
    @Transactional
    public boolean deleteMessage(Long messageId, String requesterEmail) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Verify that the user attempting to delete is the original sender
        if (message.getSender() != null && message.getSender().equals(requesterEmail)) {
            messageRepository.delete(message);
            
            // Optional (Recommended): If you want real-time deletion updates on the UI via WebSocket, 
            // you can broadcast the deleted message ID to the channel topic as well:
            // if (message.getChannel() != null) {
            //     messagingTemplate.convertAndSend("/topic/channels/" + message.getChannel().getId() + "/deleted", messageId);
            // }

            return true;
        }
        
        return false; // Returns false if the requester is not the sender (triggers 403 Forbidden in controller)
    }

    public List<KnowledgeItem> getAllKnowledge() {
        return knowledgeRepository.findAll();
    }

    @Transactional
    public KnowledgeItem createKnowledgeItem(KnowledgeItem knowledgeItem) {
        if (knowledgeItem == null || knowledgeItem.getTitle() == null || knowledgeItem.getTitle().isBlank()) {
            throw new IllegalArgumentException("Knowledge title is required");
        }

        if (knowledgeItem.getCreatedAt() == null) {
            knowledgeItem.setCreatedAt(java.time.LocalDateTime.now());
        }

        return knowledgeRepository.save(knowledgeItem);
    }

    public void deleteKnowledgeItem(Long id) {
        knowledgeRepository.deleteById(id);
    }
}

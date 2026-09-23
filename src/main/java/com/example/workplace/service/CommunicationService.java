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

import java.util.List;
import java.util.Optional;

@Service
public class CommunicationService {

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final KnowledgeRepository knowledgeRepository;

    public CommunicationService(ChannelRepository channelRepository,
                                MessageRepository messageRepository,
                                KnowledgeRepository knowledgeRepository) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.knowledgeRepository = knowledgeRepository;
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
        if (message == null || message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException("Message content is required");
        }

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        message.setChannel(channel);
        if (message.getTimestamp() == null) {
            message.setTimestamp(java.time.LocalDateTime.now());
        }
        return messageRepository.save(message);
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

package com.finc.platform.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    // Accepted in requests ({"channel":{"id":1}}) but never written back out
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Channel channel;

    @Column(nullable = false)
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String attachmentName;

    private String attachmentType;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String attachmentData;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String reactionsData;

    @Transient
    private Map<String, List<String>> reactions;

    @Column(nullable = false)
    private boolean pinned;

    private String pinnedBy;

    private LocalDateTime pinnedAt;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public Message() {
    }

    public Message(Channel channel, String sender, String content) {
        this.channel = channel;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentData() {
        return attachmentData;
    }

    public void setAttachmentData(String attachmentData) {
        this.attachmentData = attachmentData;
    }

    public Map<String, List<String>> getReactions() {
        if (reactions == null) {
            reactions = new LinkedHashMap<>();
            if (reactionsData != null && !reactionsData.isBlank()) {
                for (String entry : reactionsData.split(";")) {
                    String[] parts = entry.split(":", 2);
                    if (parts.length == 2) {
                        String emoji = decodeReactionValue(parts[0]);
                        List<String> reactors = java.util.Arrays.stream(parts[1].split(","))
                                .map(Message::decodeReactionValue)
                                .toList();
                        reactions.put(emoji, new java.util.ArrayList<>(reactors));
                    }
                }
            }
        }
        return reactions;
    }

    public void setReactions(Map<String, List<String>> reactions) {
        this.reactions = reactions == null ? new LinkedHashMap<>() : new LinkedHashMap<>(reactions);
        this.reactionsData = this.reactions.entrySet().stream()
                .map(entry -> encodeReactionValue(entry.getKey()) + ":"
                        + entry.getValue().stream().map(Message::encodeReactionValue)
                        .collect(java.util.stream.Collectors.joining(",")))
                .collect(java.util.stream.Collectors.joining(";"));
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getPinnedBy() {
        return pinnedBy;
    }

    public void setPinnedBy(String pinnedBy) {
        this.pinnedBy = pinnedBy;
    }

    public LocalDateTime getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(LocalDateTime pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    private static String encodeReactionValue(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeReactionValue(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

package com.finc.platform.service;

import com.finc.platform.entity.Channel;
import com.finc.platform.entity.ChannelType;
import com.finc.platform.entity.Message;
import com.finc.platform.repository.ChannelRepository;
import com.finc.platform.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommunicationServiceTest {

    @Test
    void reusesDirectChannelWhenMemberEmailsDifferOnlyByOrderAndCase() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
                channelRepository,
                messageRepository,
                notificationService,
                messagingTemplate
        );

        Channel existing = new Channel("John Employee", "Direct message", ChannelType.DIRECT);
        existing.setMemberEmails(List.of("john@fincacademy.com", "admin@fincacademy.com"));
        when(channelRepository.findAll()).thenReturn(List.of(existing));

        Channel request = new Channel("John", "Another direct message", ChannelType.DIRECT);
        request.setMemberEmails(List.of("ADMIN@FINCACADEMY.COM", "john@fincacademy.com"));

        assertSame(existing, service.createChannel(request));
        verify(channelRepository, never()).save(any(Channel.class));
    }

        @Test
        void storesUploadedFileInChatMessage() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
            channelRepository,
            messageRepository,
            notificationService,
            messagingTemplate
        );

        Channel channel = new Channel("general", "Project updates", ChannelType.PUBLIC);
        channel.setId(7L);
        channel.setMemberEmails(List.of("admin@fincacademy.com"));
        when(channelRepository.findById(7L)).thenReturn(Optional.of(channel));
        when(messageRepository.save(any(Message.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        byte[] fileContents = "meeting notes".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile attachment = new MockMultipartFile(
            "attachment",
            "agenda.txt",
            "text/plain",
            fileContents
        );

        Message saved = service.createAttachmentMessage(7L, "Admin User", "", attachment);

        assertEquals("agenda.txt", saved.getAttachmentName());
        assertEquals("text/plain", saved.getAttachmentType());
        assertEquals("meeting notes", new String(
            Base64.getDecoder().decode(saved.getAttachmentData()),
            StandardCharsets.UTF_8
        ));
        verify(messageRepository).save(saved);
        }

    @Test
    void togglesReactionOnAndOff() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
                channelRepository, messageRepository, notificationService, messagingTemplate);
        Channel channel = new Channel("general", "Project updates", ChannelType.PUBLIC);
        channel.setId(7L);
        Message message = new Message(channel, "John Employee", "Hello");
        message.setId(11L);
        when(messageRepository.findById(11L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message reacted = service.toggleReaction(11L, "Admin User", "👍");
        assertEquals(List.of("Admin User"), reacted.getReactions().get("👍"));

        Message unreacted = service.toggleReaction(11L, "Admin User", "👍");
        assertTrue(unreacted.getReactions().isEmpty());
        verify(messageRepository, org.mockito.Mockito.times(2)).save(message);
    }

    @Test
    void rejectsEditingAnotherUsersMessage() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
                channelRepository, messageRepository, notificationService, messagingTemplate);
        Message message = new Message(new Channel("general", "Project updates", ChannelType.PUBLIC), "John Employee", "Hello");
        message.setId(11L);
        when(messageRepository.findById(11L)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> service.editMessage(11L, "Admin User", "Changed"));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void allowsConversationMemberToPinAndUnpinMessage() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
                channelRepository, messageRepository, notificationService, messagingTemplate);
        Channel channel = new Channel("general", "Project updates", ChannelType.PUBLIC);
        channel.setId(7L);
        channel.setMemberEmails(List.of("admin@fincacademy.com"));
        Message message = new Message(channel, "John Employee", "Important update");
        message.setId(11L);
        when(messageRepository.findById(11L)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message pinned = service.togglePin(11L, "ADMIN@FINCACADEMY.COM");
        assertTrue(pinned.isPinned());
        assertEquals("ADMIN@FINCACADEMY.COM", pinned.getPinnedBy());
        assertTrue(pinned.getPinnedAt() != null);

        Message unpinned = service.togglePin(11L, "admin@fincacademy.com");
        assertTrue(!unpinned.isPinned());
        assertEquals(null, unpinned.getPinnedBy());
        assertEquals(null, unpinned.getPinnedAt());
    }

    @Test
    void rejectsPinningMessageForNonMember() {
        ChannelRepository channelRepository = mock(ChannelRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        NotificationService notificationService = mock(NotificationService.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        CommunicationService service = new CommunicationService(
                channelRepository, messageRepository, notificationService, messagingTemplate);
        Channel channel = new Channel("private", "Private discussion", ChannelType.PRIVATE);
        channel.setMemberEmails(List.of("member@fincacademy.com"));
        Message message = new Message(channel, "Member", "Private update");
        message.setId(12L);
        when(messageRepository.findById(12L)).thenReturn(Optional.of(message));

        assertThrows(SecurityException.class, () -> service.togglePin(12L, "outsider@fincacademy.com"));
        verify(messageRepository, never()).save(any(Message.class));
    }
}
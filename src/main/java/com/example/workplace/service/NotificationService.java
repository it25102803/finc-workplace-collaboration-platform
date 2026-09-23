package com.example.workplace.service;

import com.example.workplace.model.Message;
import com.example.workplace.model.Notification;
import com.example.workplace.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final String mailHost;

    public NotificationService(NotificationRepository notificationRepository,
                                SimpMessagingTemplate messagingTemplate,
                                @Autowired(required = false) JavaMailSender mailSender,
                                @Value("${spring.mail.host:}") String mailHost) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
        this.mailSender = mailSender;
        this.mailHost = mailHost;
    }

    @Transactional
    public void notifyMembers(Message message, List<String> recipients, String mention) {
        String text = mention == null
                ? message.getSender() + " sent a new message"
                : message.getSender() + " mentioned you in a message";
        String type = mention == null ? "NEW_MESSAGE" : "MENTION";

        recipients.stream()
                .filter(recipient -> recipient != null && !recipient.equalsIgnoreCase(message.getSender()))
                .distinct()
                .forEach(recipient -> createAndDispatch(recipient, type, text));
    }

    public List<Notification> getForUser(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Notification markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    private void createAndDispatch(String recipient, String type, String text) {
        Notification notification = notificationRepository.save(new Notification(recipient, type, text));
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient, notification);

        if (mailSender != null && !mailHost.isBlank()) {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(recipient);
            email.setSubject("Workplace notification");
            email.setText(text);
            mailSender.send(email);
        }
    }
}

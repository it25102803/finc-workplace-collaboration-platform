package com.example.workplace.controller;

import com.example.workplace.model.Message;
import com.example.workplace.service.CommunicationService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {

    private final CommunicationService communicationService;

    public WebSocketMessageController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @MessageMapping("/channels/{channelId}/messages")
    @SendTo("/topic/channels/{channelId}")
    public Message sendMessage(@DestinationVariable Long channelId, Message message) {
        return communicationService.createMessage(channelId, message);
    }
}
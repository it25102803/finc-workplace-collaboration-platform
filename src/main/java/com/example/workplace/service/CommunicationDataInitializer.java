package com.example.workplace.service;

import com.example.workplace.model.Channel;
import com.example.workplace.model.ChannelType;
import com.example.workplace.model.KnowledgeItem;
import com.example.workplace.model.Message;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommunicationDataInitializer implements CommandLineRunner {

    private final CommunicationService communicationService;

    public CommunicationDataInitializer(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @Override
    public void run(String... args) {
        if (communicationService.getAllChannels().isEmpty()) {
            Channel general = new Channel(
                    "general",
                    "Project updates and announcements",
                    ChannelType.PUBLIC
            );
            general.setMemberEmails(new java.util.ArrayList<>(java.util.List.of("admin@fincacademy.com", "john@fincacademy.com")));
            general = communicationService.createChannel(general);
            communicationService.createMessage(general.getId(), new Message(general, "Admin User", "Welcome to the Workplace collaboration hub."));
            communicationService.createMessage(general.getId(), new Message(general, "John Employee", "Let's finalize the team launch tasks."));

            Channel design = new Channel(
                    "design-team",
                    "Design reviews and UI feedback",
                    ChannelType.PRIVATE
            );
            design.setMemberEmails(new java.util.ArrayList<>(java.util.List.of("admin@fincacademy.com", "john@fincacademy.com")));
            design = communicationService.createChannel(design);
            communicationService.createMessage(design.getId(), new Message(design, "Admin User", "Share the latest mock-up in the design review channel."));

            communicationService.createKnowledgeItem(new KnowledgeItem(
                    "Project Kickoff Guide",
                    "Use this guide to introduce the team, goals, and communication rules for the new workplace platform.",
                    "Operations",
                    "Admin User"
            ));

            communicationService.createKnowledgeItem(new KnowledgeItem(
                    "Customer Response Template",
                    "Acknowledge the request, confirm the timeline, and include the appropriate escalation contact when needed.",
                    "Support",
                    "John Employee"
            ));
        }
    }
}

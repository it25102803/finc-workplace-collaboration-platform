package com.finc.platform.service;

import com.finc.platform.entity.Channel;
import com.finc.platform.entity.ChannelType;
import com.finc.platform.entity.Message;
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

            }

                Channel direct = new Channel(
                        "John Employee",
                        "Direct message with John Employee",
                        ChannelType.DIRECT
                );
                direct.setMemberEmails(new java.util.ArrayList<>(java.util.List.of(
                        "admin@fincacademy.com",
                        "john@fincacademy.com"
                )));
                direct = communicationService.createChannel(direct);

                if (communicationService.getMessagesForChannel(direct.getId()).isEmpty()) {
                    communicationService.createMessage(direct.getId(), new Message(
                        direct,
                        "John Employee",
                        "Can we review the channel requirements today?"
                    ));
                }

    }
}

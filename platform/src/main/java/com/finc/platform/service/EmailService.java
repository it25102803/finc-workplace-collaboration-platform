package com.finc.platform.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    private boolean smtpReady = false;

    @PostConstruct
    public void init() {
        smtpReady = mailSender != null
                && fromAddress != null
                && !fromAddress.isBlank();
        if (!smtpReady) {
            System.out.println("[EmailService] SMTP not configured — reset links will be printed to console.");
        } else {
            System.out.println("[EmailService] SMTP ready — reset links will be emailed from " + fromAddress);
        }
    }

    /**
     * Sends password reset email. Falls back to console log if SMTP not configured.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = baseUrl + "/reset-password.html?token=" + resetToken;

        if (!smtpReady) {
            // ---- DEMO FALLBACK: print to console ----
            System.out.println();
            System.out.println("========================================================");
            System.out.println(" PASSWORD RESET LINK (SMTP not configured)");
            System.out.println(" To:    " + toEmail);
            System.out.println(" Link:  " + resetLink);
            System.out.println(" Expires in 15 minutes.");
            System.out.println("========================================================");
            System.out.println();
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(toEmail);
            msg.setSubject("Finc Workplace — Password Reset");
            msg.setText(
                    "Hi,\n\n" +
                    "You requested a password reset. Click the link below (valid for 15 minutes):\n\n" +
                    resetLink + "\n\n" +
                    "If you didn't request this, ignore this email.\n\n" +
                    "— Finc Workplace Collaboration Platform"
            );
            mailSender.send(msg);
            System.out.println("[EmailService] Reset email sent to " + toEmail);
        } catch (Exception e) {
            System.out.println("[EmailService] Failed to send email: " + e.getMessage());
            System.out.println("[EmailService] Fallback reset link: " + resetLink);
        }
    }
}
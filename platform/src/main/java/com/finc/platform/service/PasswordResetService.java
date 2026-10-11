package com.finc.platform.service;

import com.finc.platform.entity.PasswordResetToken;
import com.finc.platform.entity.User;
import com.finc.platform.repository.PasswordResetTokenRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_MINUTES = 15;

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordResetTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private AuditLogService auditLogService;

    /**
     * Step 1: User requests a reset link by email.
     * Always returns silently — don't leak whether the email exists (security).
     */
    @Transactional
    public void requestReset(String email) {
        // Look up user — if not found, silently return (don't reveal existence)
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            System.out.println("[PasswordReset] No user for email: " + email + " (silently ignoring)");
            return;
        }

        // Invalidate any previous unused tokens for this user
        tokenRepository.findByUserAndUsedFalse(user).forEach(t -> {
            t.setUsed(true);
            tokenRepository.save(t);
        });

        // Generate fresh token
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken(token, user, TOKEN_VALIDITY_MINUTES);
        tokenRepository.save(prt);

        // Send it
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        // Audit
        auditLogService.log("PASSWORD_RESET_REQUESTED", user.getId(), user.getId(),
                "Reset link requested for " + user.getUsername());
    }

    /**
     * Step 2: User submits token + new password.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (prt.isUsed()) {
            throw new IllegalArgumentException("This reset link has already been used");
        }
        if (prt.isExpired()) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate token
        prt.setUsed(true);
        tokenRepository.save(prt);

        // Audit
        auditLogService.log("PASSWORD_RESET_COMPLETED", user.getId(), user.getId(),
                "Password successfully reset for " + user.getUsername());
    }

    /** Optional: validate a token before showing the form (nice UX). */
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsed() && !t.isExpired())
                .orElse(false);
    }
}
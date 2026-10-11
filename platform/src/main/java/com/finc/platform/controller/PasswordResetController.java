package com.finc.platform.controller;

import com.finc.platform.dto.ForgotPasswordRequest;
import com.finc.platform.dto.ResetPasswordRequest;
import com.finc.platform.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * POST /api/auth/forgot-password
     * Body: { "email": "user@finc.com" }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        try {
            passwordResetService.requestReset(req.getEmail());
            // Always return the same message — don't reveal whether the email exists
            return ResponseEntity.ok(Map.of(
                    "message", "If an account exists for that email, a reset link has been sent."
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "message", "If an account exists for that email, a reset link has been sent."
            ));
        }
    }

    /**
     * POST /api/auth/reset-password
     * Body: { "token": "abc-123", "newPassword": "newpass123" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password has been reset. You can now log in."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * GET /api/auth/validate-reset-token?token=abc-123
     * Used by reset-password.html to check the token before showing the form.
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        boolean valid = passwordResetService.isTokenValid(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
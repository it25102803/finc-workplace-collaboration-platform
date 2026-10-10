package com.finc.platform.controller;

import com.finc.platform.dto.AuthResponse;
import com.finc.platform.dto.LoginRequest;
import com.finc.platform.dto.RegisterRequest;
import com.finc.platform.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 1. POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. POST /api/auth/login (Fixes "No static resource api/auth/login")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. POST /api/auth/logout (FR-UM-07)
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            authService.logout(userId);
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
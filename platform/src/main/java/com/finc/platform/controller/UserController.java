package com.finc.platform.controller;

import com.finc.platform.dto.ChangePasswordRequest;
import com.finc.platform.dto.UserUpdateRequest;
import com.finc.platform.entity.User;
import com.finc.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finc.platform.service.ProfilePictureService;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.HashMap;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // NOTE: GET /api/users is handled by UserDirectoryController.
    // Do not define it here or Spring will throw "Ambiguous mapping".

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        try {
            User updated = userService.updateProfile(id,
                    req.getFirstName(), req.getLastName(), req.getEmail(),
                    req.getPhone(), req.getDepartmentId());
            return ResponseEntity.ok(Map.of("message", "Profile updated", "user", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id,
                                            @RequestBody ChangePasswordRequest req) {
        try {
            userService.changePassword(id, req.getCurrentPassword(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password changed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Autowired
    private ProfilePictureService profilePictureService;

    // FR-UM-11: upload profile picture
    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) {
        try {
            String url = profilePictureService.upload(id, file, id);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Profile picture uploaded");
            body.put("url", url);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to store file");
    }
}
}
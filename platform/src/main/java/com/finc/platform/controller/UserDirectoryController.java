package com.finc.platform.controller;

import com.finc.platform.dto.UserSummary;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserDirectoryController {

    private final UserRepository userRepository;

    public UserDirectoryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // FR-UM-09: Get all users with full entity scope (Role, Department, Status)
    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Names and emails of active users, for chat, task and meeting pickers. Open to every logged-in user.
    @GetMapping("/basic")
    public ResponseEntity<List<UserSummary>> getBasicUsers() {
        List<UserSummary> users = userRepository.findAll().stream()
                .filter(u -> !Boolean.FALSE.equals(u.getEnabled()))
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getEmail(), displayName(u)))
                .toList();
        return ResponseEntity.ok(users);
    }

    static String displayName(User u) {
        String first = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String last = u.getLastName() == null ? "" : u.getLastName().trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? u.getUsername() : full;
    }
}
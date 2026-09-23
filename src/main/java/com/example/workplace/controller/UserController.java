package com.example.workplace.controller;

import com.example.workplace.model.User;
import com.example.workplace.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        User user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable int id,
            @RequestBody User user) {

        User updatedUser = userService.updateUser(id, user);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disableUser(@PathVariable int id) {
        boolean result = userService.disableUser(id);

        if (!result) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("User disabled successfully");
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<String> resetPassword(
            @PathVariable int id,
            @RequestBody Map<String, String> request) {

        String newPassword = request.get("password");
        boolean result = userService.resetPassword(id, newPassword);

        if (!result) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        User user = userService.login(email, password);

        if (user == null) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword) {
        return userService.searchUsers(keyword);
    }
}
//test

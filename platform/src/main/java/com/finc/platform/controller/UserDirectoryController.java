package com.finc.platform.controller;

import com.finc.platform.dto.UserSummary;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserDirectoryController {

    private final UserRepository userRepository;

    public UserDirectoryController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserSummary> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    private UserSummary toSummary(User user) {
        String name = Stream.of(user.getFirstName(), user.getLastName())
                .filter(part -> part != null && !part.isBlank())
                .map(String::trim)
                .collect(Collectors.joining(" "));
        if (name.isBlank()) {
            name = user.getUsername();
        }
        return new UserSummary(user.getEmail(), name);
    }
}
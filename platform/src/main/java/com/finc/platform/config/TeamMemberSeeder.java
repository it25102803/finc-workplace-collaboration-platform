package com.finc.platform.config;

import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(2) // Runs after DataInitializer
public class TeamMemberSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public TeamMemberSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Do NOT seed admin here — DataInitializer creates the admin account
        seedMember("member1", "member1@finc.com", "Member 1", "(User Management)");
        seedMember("member2", "member2@finc.com", "Member 2", "(Communication)");
        seedMember("member3", "member3@finc.com", "Member 3", "(Task & Calendar)");
        seedMember("member4", "member4@finc.com", "Member 4", "(Files & Knowledge)");
    }

    private void seedMember(String username, String email, String first, String last) {
        // Check by email to prevent duplicate entry violations
        boolean exists = userRepository.findAll().stream()
                .anyMatch(u -> email.equalsIgnoreCase(u.getEmail()) || username.equalsIgnoreCase(u.getUsername()));

        if (!exists) {
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword("123456");
            u.setFirstName(first);
            u.setLastName(last);
            u.setStatus(UserStatus.ONLINE);
            u.setEnabled(true);
            u.setCreatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }
}
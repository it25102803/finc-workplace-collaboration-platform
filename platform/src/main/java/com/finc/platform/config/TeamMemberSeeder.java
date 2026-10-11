package com.finc.platform.config;

import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(1)
public class TeamMemberSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    public TeamMemberSeeder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        // Disabled default seeding so users are created strictly through UI registration (FR-UM-01)
        // To restore dummy users for Member 2 testing if needed, uncomment the block below:
        /*
        if (userRepository.count() == 0) {
            seedUser(1L, "admin", "admin@finc.com", "Admin", "System");
            seedUser(2L, "member1", "member1@finc.com", "Member 1", "(User Management)");
            seedUser(3L, "member2", "member2@finc.com", "Member 2", "(Communication)");
            seedUser(4L, "member3", "member3@finc.com", "Member 3", "(Task & Calendar)");
            seedUser(5L, "member4", "member4@finc.com", "Member 4", "(Files & Knowledge)");
        }
        */
    }

    private void seedUser(Long id, String username, String email, String first, String last) {
        if (!userRepository.existsById(id)) {
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
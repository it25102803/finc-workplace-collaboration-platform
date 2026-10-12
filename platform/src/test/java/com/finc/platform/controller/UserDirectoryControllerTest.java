package com.finc.platform.controller;

import com.finc.platform.dto.UserSummary;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDirectoryControllerTest {

    private User user(Long id, String username, String email, String first, String last, boolean enabled) {
        User u = new User(username, email, "secret", first, last);
        u.setId(id);
        u.setEnabled(enabled);
        return u;
    }

    @Test
    void returnsAllUsersForTheAdminDirectory() {
        UserRepository userRepository = mock(UserRepository.class);
        UserDirectoryController controller = new UserDirectoryController(userRepository);
        List<User> users = List.of(
                user(1L, "admin", "admin@fincacademy.com", "Admin", "User", true),
                user(2L, "john", "john@fincacademy.com", null, null, true));
        when(userRepository.findAll()).thenReturn(users);

        assertEquals(users, controller.getUsers().getBody());
    }

    @Test
    void basicListHasSafeDisplayNamesAndSkipsDisabledUsers() {
        UserRepository userRepository = mock(UserRepository.class);
        UserDirectoryController controller = new UserDirectoryController(userRepository);
        when(userRepository.findAll()).thenReturn(List.of(
                user(1L, "admin", "admin@fincacademy.com", "Admin", "User", true),
                user(2L, "john", "john@fincacademy.com", null, null, true),
                user(3L, "gone", "gone@fincacademy.com", "Gone", "User", false)));

        assertEquals(List.of(
                new UserSummary(1L, "admin", "admin@fincacademy.com", "Admin User"),
                new UserSummary(2L, "john", "john@fincacademy.com", "john")
        ), controller.getBasicUsers().getBody());
    }
}
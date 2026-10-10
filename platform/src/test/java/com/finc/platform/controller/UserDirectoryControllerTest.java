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

    @Test
    void returnsSafeDisplayNamesForPersistedUsers() {
        UserRepository userRepository = mock(UserRepository.class);
        UserDirectoryController controller = new UserDirectoryController(userRepository);
        when(userRepository.findAll()).thenReturn(List.of(
                new User("admin", "admin@fincacademy.com", "secret", "Admin", "User"),
                new User("john", "john@fincacademy.com", "secret", null, null)
        ));

        assertEquals(List.of(
                new UserSummary("admin@fincacademy.com", "Admin User"),
                new UserSummary("john@fincacademy.com", "john")
        ), controller.getUsers());
    }
}
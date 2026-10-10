package com.finc.platform.service;

import com.finc.platform.entity.UserEmail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final List<UserEmail> users = new ArrayList<>();

    private int nextId = 1;

    public UserService() {
        users.add(new UserEmail(
                nextId++,
                "Admin User",
                "admin@fincacademy.com",
                "admin123",
                "Management",
                "SYSTEM_ADMINISTRATOR"
        ));

        users.add(new UserEmail(
                nextId++,
                "John Employee",
                "john@fincacademy.com",
                "123456",
                "Finance",
                "EMPLOYEE"
        ));
    }

    public List<UserEmail> getAllUsers() {
        return users;
    }

    public UserEmail getUserById(int id) {
        for (UserEmail user : users) {
            if (user.getId() == id) {
                return user;
            }
        }

        return null;
    }

    public UserEmail createUser(UserEmail user) {
        user.setId(nextId++);
        user.setActive(true);
        user.setOnline(false);

        users.add(user);

        return user;
    }

    public UserEmail updateUser(int id, UserEmail updatedUser) {
        UserEmail existingUser = getUserById(id);

        if (existingUser == null) {
            return null;
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setDepartment(updatedUser.getDepartment());
        existingUser.setRole(updatedUser.getRole());

        return existingUser;
    }

    public boolean disableUser(int id) {
        UserEmail user = getUserById(id);

        if (user == null) {
            return false;
        }

        user.setActive(false);

        return true;
    }

    public boolean resetPassword(int id, String newPassword) {
        UserEmail user = getUserById(id);

        if (user == null) {
            return false;
        }

        user.setPassword(newPassword);

        return true;
    }

    public UserEmail login(String email, String password) {
        for (UserEmail user : users) {
            if (user.getEmail().equals(email)
                    && user.getPassword().equals(password)
                    && user.isActive()) {

                user.setOnline(true);
                return user;
            }
        }

        return null;
    }

    public List<UserEmail> searchUsers(String keyword) {
        List<UserEmail> result = new ArrayList<>();

        for (UserEmail user : users) {
            if (user.getName().toLowerCase().contains(keyword.toLowerCase())
                    || user.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(user);
            }
        }

        return result;
    }
}

package com.example.workplace.service;

import com.example.workplace.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();

    private int nextId = 1;

    public UserService() {
        users.add(new User(
                nextId++,
                "Admin User",
                "admin@fincacademy.com",
                "admin123",
                "Management",
                "SYSTEM_ADMINISTRATOR"
        ));

        users.add(new User(
                nextId++,
                "John Employee",
                "john@fincacademy.com",
                "123456",
                "Finance",
                "EMPLOYEE"
        ));
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User getUserById(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }

        return null;
    }

    public User createUser(User user) {
        user.setId(nextId++);
        user.setActive(true);
        user.setOnline(false);

        users.add(user);

        return user;
    }

    public User updateUser(int id, User updatedUser) {
        User existingUser = getUserById(id);

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
        User user = getUserById(id);

        if (user == null) {
            return false;
        }

        user.setActive(false);

        return true;
    }

    public boolean resetPassword(int id, String newPassword) {
        User user = getUserById(id);

        if (user == null) {
            return false;
        }

        user.setPassword(newPassword);

        return true;
    }

    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email)
                    && user.getPassword().equals(password)
                    && user.isActive()) {

                user.setOnline(true);
                return user;
            }
        }

        return null;
    }

    public List<User> searchUsers(String keyword) {
        List<User> result = new ArrayList<>();

        for (User user : users) {
            if (user.getName().toLowerCase().contains(keyword.toLowerCase())
                    || user.getEmail().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(user);
            }
        }

        return result;
    }
}

package com.finc.platform.service;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuditLogService auditLogService;
    @Autowired private SystemSettingService systemSettingService;

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllUsers();
        String k = keyword.toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(k)
                        || u.getEmail().toLowerCase().contains(k)
                        || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(k))
                        || (u.getLastName() != null && u.getLastName().toLowerCase().contains(k)))
                .toList();
    }

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName,
                              String email, String phone, Long departmentId) {
        User user = getUserById(userId);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phone != null) user.setPhone(phone);

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(email);
        }
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            user.setDepartment(dept);
        }

        User saved = userRepository.save(user);
        auditLogService.log("UPDATE_PROFILE", userId, userId, "User updated own profile");
        return saved;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        systemSettingService.validatePasswordPolicy(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log("CHANGE_PASSWORD", userId, userId, "Password changed");
    }

    @Transactional
    public User setStatus(Long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        return userRepository.save(user);
    }
}
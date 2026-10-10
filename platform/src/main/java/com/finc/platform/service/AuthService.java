package com.finc.platform.service;

import com.finc.platform.dto.AuthResponse;
import com.finc.platform.dto.LoginRequest;
import com.finc.platform.dto.RegisterRequest;
import com.finc.platform.entity.Role;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.RoleRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * User Registration (FR-UM-01, FR-UM-02, FR-UM-03, FR-UM-04)
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // BCrypt Password Hashing (NFR-01)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setStatus(UserStatus.OFFLINE);

        // Assign default ROLE_EMPLOYEE (FR-UM-03)
        Role defaultRole = roleRepository.findByRoleType(RoleType.ROLE_EMPLOYEE)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_EMPLOYEE);
                    return roleRepository.save(r);
                });
        user.setRole(defaultRole);

        // Optional Department allocation (FR-UM-04)
        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);
        return mapToAuthResponse(savedUser);
    }

    /**
     * User Login Method (FR-UM-05, FR-UM-06)
     * FIXES: "The method login(LoginRequest) is undefined for the type AuthService"
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // Verify BCrypt hashed password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // Set status to ONLINE upon successful login (FR-UM-06)
        user.setStatus(UserStatus.ONLINE);
        User updatedUser = userRepository.save(user);

        return mapToAuthResponse(updatedUser);
    }

    /**
     * Session Logout Method (FR-UM-07)
     */
    public void logout(Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.OFFLINE);
                userRepository.save(user);
            });
        }
    }

    /**
     * Helper to map User Entity to AuthResponse DTO with clean 3-digit numerical ID
     */
    private AuthResponse mapToAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        
        // Formats ID to clean 3 digits ("001", "002", "003")
        String formattedBadge = String.format("%03d", user.getId());
        response.setBadgeId(formattedBadge);

        if (user.getRole() != null) {
            response.setRole(user.getRole().getRoleType().name());
        } else {
            response.setRole("ROLE_EMPLOYEE");
        }

        return response;
    }
}
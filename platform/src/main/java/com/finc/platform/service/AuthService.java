package com.finc.platform.service;

import com.finc.platform.dto.AuthResponse;
import com.finc.platform.dto.LoginRequest;
import com.finc.platform.dto.RegisterRequest;
import com.finc.platform.entity.Department;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already in use");
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

        // Department allocation handling (FR-UM-04)
        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);
        return mapToAuthResponse(savedUser);
    }

    /**
     * User Login Method (FR-UM-05, FR-UM-06)
     */
    @Transactional
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
    @Transactional
    public void logout(Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.OFFLINE);
                userRepository.save(user);
            });
        }
    }

    /**
     * Dynamic Role Provisioning (FR-UM-03)
     */
    @Transactional
    public User updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        RoleType roleType = RoleType.valueOf(roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName);
        Role role = roleRepository.findByRoleType(roleType)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(roleType);
                    return roleRepository.save(r);
                });

        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Department Allocation / Re-allocation (FR-UM-04)
     */
    @Transactional
    public User updateUserDepartment(Long userId, Long departmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            user.setDepartment(department);
        } else {
            user.setDepartment(null);
        }

        return userRepository.save(user);
    }

    /**
     * User Status Syncing & Account Governance (FR-UM-06, FR-UM-09)
     */
    @Transactional
    public User updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(status);
        return userRepository.save(user);
    }

    /**
     * Helper to map User Entity to AuthResponse DTO with clean 3-digit numerical ID
     */
    private AuthResponse mapToAuthResponse(User user) {
        AuthResponse response = new AuthResponse();
        
        // Supports primary key mapping regardless of getId() method structure
        Long id = user.getId();
        response.setUserId(id);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        
        // Formats ID to clean 3-digit badge string ("001", "002", "003")
        String formattedBadge = String.format("%03d", id != null ? id : 1);
        response.setBadgeId(formattedBadge);

        if (user.getRole() != null) {
            response.setRole(user.getRole().getRoleType().name());
        } else {
            response.setRole("ROLE_EMPLOYEE");
        }

        if (user.getDepartment() != null) {
            response.setDepartmentName(user.getDepartment().getName());
        } else {
            response.setDepartmentName("Software Engineering");
        }

        return response;
    }
}
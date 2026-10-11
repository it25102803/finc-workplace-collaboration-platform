package com.finc.platform.service;

import com.finc.platform.dto.AdminCreateUserRequest;
import com.finc.platform.dto.AdminUpdateUserRequest;
import com.finc.platform.entity.AuditLog;
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
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuditLogService auditLogService;

    @Transactional
    public User createUser(AdminCreateUserRequest req, Long actorId) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhone(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(req.getEnabled() == null || req.getEnabled());
        user.setStatus(UserStatus.OFFLINE);

        Role role = resolveRole(req.getRole(), RoleType.ROLE_EMPLOYEE);
        user.setRole(role);

        if (req.getDepartmentId() != null) {
            Department d = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            user.setDepartment(d);
        }

        User saved = userRepository.save(user);
        auditLogService.log("ADMIN_CREATE_USER", actorId, saved.getId(),
                "Created user " + saved.getUsername() + " with role " + role.getRoleType());
        return saved;
    }

    @Transactional
    public User updateUser(Long userId, AdminUpdateUserRequest req, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        if (req.getDepartmentId() != null) {
            Department d = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            user.setDepartment(d);
        }
        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
            if (!req.getEnabled()) user.setStatus(UserStatus.OFFLINE);
        }

        User saved = userRepository.save(user);
        auditLogService.log("ADMIN_UPDATE_USER", actorId, saved.getId(),
                "Updated user " + saved.getUsername());
        return saved;
    }

    @Transactional
    public User setEnabled(Long userId, boolean enabled, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(enabled);
        if (!enabled) user.setStatus(UserStatus.OFFLINE);
        User saved = userRepository.save(user);
        auditLogService.log(enabled ? "ADMIN_ENABLE_USER" : "ADMIN_DISABLE_USER",
                actorId, user.getId(), (enabled ? "Enabled " : "Disabled ") + user.getUsername());
        return saved;
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword, Long actorId) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        auditLogService.log("ADMIN_RESET_PASSWORD", actorId, user.getId(),
                "Reset password for " + user.getUsername());
    }

    @Transactional
    public User assignRole(Long userId, String roleName, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Role role = resolveRole(roleName, RoleType.ROLE_EMPLOYEE);
        user.setRole(role);
        User saved = userRepository.save(user);
        auditLogService.log("ADMIN_ASSIGN_ROLE", actorId, user.getId(),
                "Assigned role " + role.getRoleType() + " to " + user.getUsername());
        return saved;
    }

    public List<AuditLog> recentAuditLogs() {
        return auditLogService.all();
    }

    public Map<String, Long> stats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "enabledUsers", userRepository.countByEnabledTrue(),
                "totalDepartments", departmentRepository.count(),
                "totalRoles", roleRepository.count()
        );
    }

    private Role resolveRole(String roleName, RoleType fallback) {
        RoleType type = fallback;
        if (roleName != null && !roleName.isBlank()) {
            String normalized = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            try {
                type = RoleType.valueOf(normalized);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + roleName);
            }
        }
        final RoleType finalType = type;
        return roleRepository.findByRoleType(finalType).orElseGet(() -> {
            Role r = new Role();
            r.setRoleType(finalType);
            return roleRepository.save(r);
        });
    }
}
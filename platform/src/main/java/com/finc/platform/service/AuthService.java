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

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuditLogService auditLogService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Empty fields must never create an account (an empty username would let people log in with only a password)
        if (isBlank(request.getUsername()) || isBlank(request.getEmail())
                || isBlank(request.getPassword()) || isBlank(request.getFirstName())
                || isBlank(request.getLastName())) {
            throw new IllegalArgumentException("Please fill in all required fields");
        }
        request.setUsername(request.getUsername().trim());
        request.setEmail(request.getEmail().trim());

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
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setStatus(UserStatus.OFFLINE);

        Role defaultRole = roleRepository.findByRoleType(RoleType.ROLE_EMPLOYEE)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_EMPLOYEE);
                    return roleRepository.save(r);
                });
        user.setRole(defaultRole);

        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .ifPresent(user::setDepartment);
        }

        User savedUser = userRepository.save(user);

        auditLogService.log("REGISTER", savedUser.getId(), savedUser.getId(),
                "New user registered: " + savedUser.getUsername());

        return mapToAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new IllegalArgumentException("Your account has been disabled. Contact the administrator.");
        }

        user.setStatus(UserStatus.ONLINE);
        user.setLastSeen(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        auditLogService.log("LOGIN", updatedUser.getId(), updatedUser.getId(),
                "User logged in: " + updatedUser.getUsername());

        return mapToAuthResponse(updatedUser);
    }

    @Transactional
    public void logout(Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.OFFLINE);
                user.setLastSeen(LocalDateTime.now());
                userRepository.save(user);

                auditLogService.log("LOGOUT", user.getId(), user.getId(),
                        "User logged out: " + user.getUsername());
            });
        }
    }

    @Transactional
    public User updateUserRole(Long userId, String roleName, Long actorId) {
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
        User saved = userRepository.save(user);
        auditLogService.log("ASSIGN_ROLE", actorId, user.getId(),
                "Role changed to " + roleType.name() + " for " + user.getUsername());
        return saved;
    }

    @Transactional
    public User updateUserRole(Long userId, String roleName) {
        return updateUserRole(userId, roleName, null);
    }

    @Transactional
    public User updateUserDepartment(Long userId, Long departmentId, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newDeptName;
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            user.setDepartment(department);
            newDeptName = department.getName();
        } else {
            user.setDepartment(null);
            newDeptName = "None";
        }

        User saved = userRepository.save(user);
        auditLogService.log("UPDATE_DEPARTMENT", actorId, user.getId(),
                "Department changed to " + newDeptName + " for " + user.getUsername());
        return saved;
    }

    @Transactional
    public User updateUserDepartment(Long userId, Long departmentId) {
        return updateUserDepartment(userId, departmentId, null);
    }

    @Transactional
    public User updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(status);
        return userRepository.save(user);
    }

    @Transactional
    public User setEnabled(Long userId, boolean enabled, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(enabled);
        if (!enabled) user.setStatus(UserStatus.OFFLINE);
        User saved = userRepository.save(user);

        auditLogService.log(enabled ? "ENABLE_USER" : "DISABLE_USER", actorId, user.getId(),
                "Account " + (enabled ? "enabled" : "disabled") + " for " + user.getUsername());
        return saved;
    }

    @Transactional
    public void adminResetPassword(Long userId, String newPassword, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditLogService.log("ADMIN_RESET_PASSWORD", actorId, user.getId(),
                "Password reset by admin for " + user.getUsername());
    }

    private AuthResponse mapToAuthResponse(User user) {
        AuthResponse response = new AuthResponse();

        Long id = user.getId();
        response.setUserId(id);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());

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
            response.setDepartmentName("Not Assigned");
        }

        return response;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
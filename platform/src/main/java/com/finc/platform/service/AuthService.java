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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public AuthService(UserRepository userRepository, 
                       RoleRepository roleRepository, 
                       DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Raw password for now until PasswordEncoder is injected
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setStatus(UserStatus.ONLINE);

     // Assign default ROLE_EMPLOYEE role
Role defaultRole = roleRepository.findByRoleType(RoleType.ROLE_EMPLOYEE)
        .orElseGet(() -> {
            Role role = new Role();
            role.setRoleType(RoleType.ROLE_EMPLOYEE);
            return roleRepository.save(role);
        });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        // Assign Department if provided
        if (registerRequest.getDepartmentId() != null) {
            Department department = departmentRepository.findById(registerRequest.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Error: Department not found."));
            user.setDepartment(department);
        }

        userRepository.save(user);

        return new AuthResponse(null, user.getUsername(), user.getEmail(), "User registered successfully!");
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: Invalid username or password."));

        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Error: Invalid username or password.");
        }

        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);

return new AuthResponse("MOCK_JWT_TOKEN_" + user.getUsername(), user.getUsername(), user.getEmail(), "Login successful!");    }
}
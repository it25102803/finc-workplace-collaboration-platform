package com.finc.platform.config;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.Role;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.RoleRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        // 1. Seed Roles (FR-UM-03)
        Role adminRole = roleRepository.findByRoleType(RoleType.ROLE_SYSTEM_ADMIN)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_SYSTEM_ADMIN);
                    return roleRepository.save(r);
                });

        Role deptAdmin = roleRepository.findByRoleType(RoleType.ROLE_DEPT_ADMIN)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_DEPT_ADMIN);
                    return roleRepository.save(r);
                });

        Role teamLeader = roleRepository.findByRoleType(RoleType.ROLE_TEAM_LEADER)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_TEAM_LEADER);
                    return roleRepository.save(r);
                });

        Role empRole = roleRepository.findByRoleType(RoleType.ROLE_EMPLOYEE)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleType(RoleType.ROLE_EMPLOYEE);
                    return roleRepository.save(r);
                });

        // 2. Seed Departments (FR-UM-04)
        if (departmentRepository.count() == 0) {
            departmentRepository.saveAll(List.of(
                    new Department("Software Engineering"),
                    new Department("Human Resources"),
                    new Department("Finance & Accounting"),
                    new Department("Marketing & Enterprise Sales")
            ));
        }

        // 3. Seed default System Administrator (only if no admin exists)
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() != null
                        && u.getRole().getRoleType() == RoleType.ROLE_SYSTEM_ADMIN);

        if (!adminExists) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@finc.com");
            admin.setPassword(passwordEncoder.encode("admin123"));   // BCrypt-hashed
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEnabled(true);
            admin.setStatus(UserStatus.OFFLINE);
            admin.setRole(adminRole);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);

            System.out.println("========================================================");
            System.out.println(" Default admin created:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println(" Change this password after first login!");
            System.out.println("========================================================");
        }
    }
}
package com.finc.platform.config;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.Role;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.entity.UserStatus;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.RoleRepository;
import com.finc.platform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@12345}")
    private String adminPassword;

    @Value("${app.admin.email:admin@finc.local}")
    private String adminEmail;

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
    public void run(String... args) {

        // 1. Seed Roles (FR-UM-03)
        Role adminRole = ensureRole(RoleType.ROLE_SYSTEM_ADMIN);
        ensureRole(RoleType.ROLE_DEPT_ADMIN);
        ensureRole(RoleType.ROLE_TEAM_LEADER);
        ensureRole(RoleType.ROLE_EMPLOYEE);

        // 2. Seed Departments (FR-UM-04)
        if (departmentRepository.count() == 0) {
            departmentRepository.saveAll(List.of(
                    new Department("Software Engineering"),
                    new Department("Human Resources"),
                    new Department("Finance & Accounting"),
                    new Department("Marketing & Enterprise Sales")
            ));
            log.info("Seeded default departments.");
        }

        // 3. Seed default System Administrator (only if none exists)
        boolean adminExists = userRepository.findByUsername(adminUsername).isPresent();

        if (adminExists) {
            log.info("Default admin '{}' already exists — skipping seed.", adminUsername);
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEnabled(true);
        admin.setStatus(UserStatus.OFFLINE);
        admin.setRole(adminRole);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        log.info("Default System Administrator seeded. Username: {}", adminUsername);
        log.info("Change the password after first login (My Profile or Forgot Password).");
        if ("Admin@12345".equals(adminPassword)) {
            log.warn("Using default seed password. Override app.admin.password in application-local.properties.");
        }
    }

    private Role ensureRole(RoleType type) {
        return roleRepository.findByRoleType(type).orElseGet(() -> {
            Role r = new Role();
            r.setRoleType(type);
            return roleRepository.save(r);
        });
    }
}
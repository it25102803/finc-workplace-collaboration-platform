package com.finc.platform.config;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.Role;
import com.finc.platform.entity.RoleType;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public DataInitializer(RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Roles into MySQL if missing (FR-UM-03)
        if (roleRepository.findByRoleType(RoleType.ROLE_SYSTEM_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setRoleType(RoleType.ROLE_SYSTEM_ADMIN);
            roleRepository.save(adminRole);
        }
        if (roleRepository.findByRoleType(RoleType.ROLE_EMPLOYEE).isEmpty()) {
            Role empRole = new Role();
            empRole.setRoleType(RoleType.ROLE_EMPLOYEE);
            roleRepository.save(empRole);
        }

        // 2. Seed Workspace Departments into MySQL if missing (FR-UM-04)
        if (departmentRepository.count() == 0) {
            departmentRepository.saveAll(List.of(
                new Department("Software Engineering"),
                new Department("Human Resources"),
                new Department("Finance & Accounting"),
                new Department("Marketing & Enterprise Sales")
            ));
            System.out.println("DataInitializer: Roles and Departments initialized cleanly.");
        }

    }
}
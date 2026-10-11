package com.finc.platform.service;

import com.finc.platform.entity.Role;
import com.finc.platform.repository.RoleRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    /** Permission catalogue — displayed in the admin UI. */
    public static final List<String> ALL_PERMISSIONS = List.of(
            "CREATE_USER", "EDIT_USER", "DISABLE_USER", "DELETE_USER",
            "RESET_PASSWORD", "ASSIGN_ROLE",
            "MANAGE_DEPARTMENT", "MANAGE_SETTINGS", "MANAGE_STORAGE_QUOTA",
            "MANAGE_CHANNEL", "MANAGE_KNOWLEDGE",
            "VIEW_AUDIT_LOG"
    );

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogService auditLogService;

    public List<Role> all() {
        return roleRepository.findAll();
    }

    public Role get(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
    }

    public List<String> catalogue() {
        return ALL_PERMISSIONS;
    }

    @Transactional
    public Role updatePermissions(Long roleId, Set<String> newPermissions, Long actorId) {
        Role role = get(roleId);

        // Reject unknown permission strings
        if (newPermissions != null) {
            for (String p : newPermissions) {
                if (!ALL_PERMISSIONS.contains(p)) {
                    throw new IllegalArgumentException("Unknown permission: " + p);
                }
            }
        }

        role.setPermissions(newPermissions != null ? new HashSet<>(newPermissions) : new HashSet<>());
        Role saved = roleRepository.save(role);

        auditLogService.log("UPDATE_ROLE_PERMISSIONS", actorId, null,
                "Role " + role.getRoleType() + " permissions set to " + saved.getPermissions());
        return saved;
    }
}
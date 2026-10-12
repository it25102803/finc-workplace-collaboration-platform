package com.finc.platform.service;

import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;

import org.springframework.stereotype.Service;

// Same rule RoleGuardFilter applies to /api/kb changes, kept here too so the Knowledge Base stays protected on its own.
@Service
public class KnowledgePermissions {

    private final UserRepository userRepository;

    public KnowledgePermissions(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireEditor(Long actorId) {
        if (actorId == null) {
            throw new SecurityException("Log in to make changes in the Knowledge Base");
        }
        User user = userRepository.findById(actorId)
                .orElseThrow(() -> new SecurityException("Unknown user: " + actorId));
        RoleType role = user.getRole() == null ? null : user.getRole().getRoleType();
        if (role != RoleType.ROLE_SYSTEM_ADMIN && role != RoleType.ROLE_DEPT_ADMIN) {
            throw new SecurityException("Only department or system administrators can change the Knowledge Base");
        }
        return user;
    }
}

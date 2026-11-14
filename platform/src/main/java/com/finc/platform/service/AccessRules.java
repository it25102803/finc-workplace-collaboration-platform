package com.finc.platform.service;

import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;

// Small shared checks for "who may do what" rules (roles and departments).
public final class AccessRules {

    private AccessRules() {
    }

    public static RoleType roleOf(User user) {
        return user == null || user.getRole() == null ? null : user.getRole().getRoleType();
    }

    public static boolean isSystemAdmin(User user) {
        return roleOf(user) == RoleType.ROLE_SYSTEM_ADMIN;
    }

    public static boolean isSameUser(User a, User b) {
        return a != null && b != null && a.getId() != null && a.getId().equals(b.getId());
    }

    // True only when both users belong to the same (non-empty) department.
    public static boolean sameDepartment(User a, User b) {
        if (a == null || b == null || a.getDepartment() == null || b.getDepartment() == null) {
            return false;
        }
        Long first = a.getDepartment().getDepartmentId();
        return first != null && first.equals(b.getDepartment().getDepartmentId());
    }
}

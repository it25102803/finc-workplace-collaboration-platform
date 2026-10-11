package com.finc.platform.dto;

import java.util.Set;

public class RolePermissionsRequest {
    private Set<String> permissions;

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
}
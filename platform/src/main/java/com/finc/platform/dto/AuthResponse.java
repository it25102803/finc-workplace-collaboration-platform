package com.finc.platform.dto;

public class AuthResponse {
    private String message;
    private Long userId;
    private String badgeId; // Formatted numerical ID ("001")
    private String username;
    private String email;
    private String role;
    private String departmentName;

    public AuthResponse() {}

    public AuthResponse(String message, Long userId, String badgeId, String username, String email, String role, String departmentName) {
        this.message = message;
        this.userId = userId;
        this.badgeId = badgeId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.departmentName = departmentName;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getBadgeId() { return badgeId; }
    public void setBadgeId(String badgeId) { this.badgeId = badgeId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}
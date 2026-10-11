package com.finc.platform.controller;

import com.finc.platform.dto.SettingUpdateRequest;
import com.finc.platform.dto.StorageQuotaRequest;
import com.finc.platform.entity.Department;
import com.finc.platform.entity.SystemSetting;
import com.finc.platform.service.DepartmentQuotaService;
import com.finc.platform.service.SystemSettingService;
import com.finc.platform.dto.AdminCreateUserRequest;
import com.finc.platform.dto.AdminResetPasswordRequest;
import com.finc.platform.dto.AdminUpdateUserRequest;
import com.finc.platform.dto.AssignRoleRequest;
import com.finc.platform.entity.AuditLog;
import com.finc.platform.entity.User;
import com.finc.platform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private DepartmentQuotaService departmentQuotaService;

    // ============ User Management (FR-UM-13 → FR-UM-17) ============

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminCreateUserRequest req,
                                        @RequestParam(required = false) Long actorId) {
        try {
            User u = adminService.createUser(req, actorId);
            return ResponseEntity.ok(Map.of("message", "User created", "user", u));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody AdminUpdateUserRequest req,
                                        @RequestParam(required = false) Long actorId) {
        try {
            User u = adminService.updateUser(id, req, actorId);
            return ResponseEntity.ok(Map.of("message", "User updated", "user", u));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/enabled")
    public ResponseEntity<?> setEnabled(@PathVariable Long id,
                                        @RequestParam boolean enabled,
                                        @RequestParam(required = false) Long actorId) {
        try {
            User u = adminService.setEnabled(id, enabled, actorId);
            return ResponseEntity.ok(Map.of(
                    "message", "User " + (enabled ? "enabled" : "disabled"),
                    "user", u));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestBody AdminResetPasswordRequest req,
                                           @RequestParam(required = false) Long actorId) {
        try {
            adminService.resetPassword(id, req.getNewPassword(), actorId);
            return ResponseEntity.ok(Map.of("message", "Password reset"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> assignRole(@PathVariable Long id,
                                        @RequestBody AssignRoleRequest req,
                                        @RequestParam(required = false) Long actorId) {
        try {
            User u = adminService.assignRole(id, req.getRole(), actorId);
            return ResponseEntity.ok(Map.of("message", "Role assigned", "user", u));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============ Audit / Stats ============

    @GetMapping("/audit-logs")
    public List<AuditLog> auditLogs() {
        return adminService.recentAuditLogs();
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return adminService.stats();
    }

    // ============ FR-UM-22: System Settings ============

    @GetMapping("/settings")
    public List<SystemSetting> listSettings() {
        return systemSettingService.all();
    }

    @PutMapping("/settings/{key}")
    public ResponseEntity<?> updateSetting(@PathVariable String key,
                                           @RequestBody SettingUpdateRequest req,
                                           @RequestParam(required = false) Long actorId) {
        try {
            SystemSetting s = systemSettingService.update(key, req.getValue(), actorId);
            return ResponseEntity.ok(Map.of("message", "Setting updated", "setting", s));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ============ FR-UM-21: Department Storage Quotas ============

    @GetMapping("/departments/quotas")
    public List<Department> listDepartmentQuotas() {
        return departmentQuotaService.all();
    }

    @PatchMapping("/departments/{id}/quota")
    public ResponseEntity<?> updateQuota(@PathVariable Long id,
                                         @RequestBody StorageQuotaRequest req,
                                         @RequestParam(required = false) Long actorId) {
        try {
            Department d = departmentQuotaService.setQuota(id, req.getLimitMb(), actorId);
            return ResponseEntity.ok(Map.of("message", "Quota updated", "department", d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
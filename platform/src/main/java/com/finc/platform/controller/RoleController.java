package com.finc.platform.controller;

import com.finc.platform.dto.RolePermissionsRequest;
import com.finc.platform.entity.Role;
import com.finc.platform.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired private RoleService roleService;

    @GetMapping
    public List<Role> all() {
        return roleService.all();
    }

    @GetMapping("/catalogue")
    public List<String> catalogue() {
        return roleService.catalogue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try { return ResponseEntity.ok(roleService.get(id)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<?> updatePermissions(@PathVariable Long id,
                                               @RequestBody RolePermissionsRequest req,
                                               @RequestParam(required = false) Long actorId) {
        try {
            Role r = roleService.updatePermissions(id, req.getPermissions(), actorId);
            return ResponseEntity.ok(Map.of("message", "Permissions updated", "role", r));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
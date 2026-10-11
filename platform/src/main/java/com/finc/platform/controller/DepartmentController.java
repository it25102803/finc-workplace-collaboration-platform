package com.finc.platform.controller;

import com.finc.platform.dto.DepartmentRequest;
import com.finc.platform.entity.Department;
import com.finc.platform.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // FR-UM-04: load available departments for signup dropdown + directory
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentService.all();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try { return ResponseEntity.ok(departmentService.get(id)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // FR-UM-18: create
    @PostMapping
    public ResponseEntity<?> create(@RequestBody DepartmentRequest req,
                                    @RequestParam(required = false) Long actorId) {
        try {
            Department d = departmentService.create(req, actorId);
            return ResponseEntity.ok(Map.of("message", "Department created", "department", d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // FR-UM-18: edit
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody DepartmentRequest req,
                                    @RequestParam(required = false) Long actorId) {
        try {
            Department d = departmentService.update(id, req, actorId);
            return ResponseEntity.ok(Map.of("message", "Department updated", "department", d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // FR-UM-18: delete (with guard)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @RequestParam(required = false) Long actorId) {
        try {
            departmentService.delete(id, actorId);
            return ResponseEntity.ok(Map.of("message", "Department deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
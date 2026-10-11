package com.finc.platform.service;

import com.finc.platform.dto.DepartmentRequest;
import com.finc.platform.entity.Department;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogService auditLogService;

    public List<Department> all() {
        return departmentRepository.findAll();
    }

    public Department get(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    @Transactional
    public Department create(DepartmentRequest req, Long actorId) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Department name is required");
        }
        departmentRepository.findAll().stream()
                .filter(d -> d.getName().equalsIgnoreCase(req.getName().trim()))
                .findFirst()
                .ifPresent(d -> { throw new IllegalArgumentException("Department name already exists"); });

        Department d = new Department(req.getName().trim(), req.getDescription());
        d.setStorageLimitMb(req.getStorageLimitMb() != null ? req.getStorageLimitMb() : 1024L);
        d.setStorageUsedMb(0L);
        Department saved = departmentRepository.save(d);

        auditLogService.log("CREATE_DEPARTMENT", actorId, null,
                "Created department '" + saved.getName() + "'");
        return saved;
    }

    @Transactional
    public Department update(Long id, DepartmentRequest req, Long actorId) {
        Department d = get(id);
        if (req.getName() != null && !req.getName().isBlank()) {
            String newName = req.getName().trim();
            if (!newName.equalsIgnoreCase(d.getName())) {
                departmentRepository.findAll().stream()
                        .filter(x -> !x.getDepartmentId().equals(id))
                        .filter(x -> x.getName().equalsIgnoreCase(newName))
                        .findFirst()
                        .ifPresent(x -> { throw new IllegalArgumentException("Department name already exists"); });
                d.setName(newName);
            }
        }
        if (req.getDescription() != null) d.setDescription(req.getDescription());
        if (req.getStorageLimitMb() != null) {
            long used = d.getStorageUsedMb() != null ? d.getStorageUsedMb() : 0L;
            if (req.getStorageLimitMb() < used) {
                throw new IllegalArgumentException("New quota is below current usage (" + used + " MB)");
            }
            d.setStorageLimitMb(req.getStorageLimitMb());
        }

        Department saved = departmentRepository.save(d);
        auditLogService.log("UPDATE_DEPARTMENT", actorId, null,
                "Updated department '" + saved.getName() + "'");
        return saved;
    }

    @Transactional
    public void delete(Long id, Long actorId) {
        Department d = get(id);

        // FR-UM-18: cannot delete a department that still has users
        long userCount = userRepository.countByDepartment_DepartmentId(id);
        if (userCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete department '" + d.getName() + "' — it still has " + userCount + " user(s). " +
                    "Reassign them to another department first.");
        }

        departmentRepository.delete(d);
        auditLogService.log("DELETE_DEPARTMENT", actorId, null,
                "Deleted department '" + d.getName() + "'");
    }
}
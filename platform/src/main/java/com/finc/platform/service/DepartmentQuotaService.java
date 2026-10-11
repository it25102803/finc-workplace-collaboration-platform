package com.finc.platform.service;

import com.finc.platform.entity.Department;
import com.finc.platform.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentQuotaService {

    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AuditLogService auditLogService;

    public List<Department> all() {
        return departmentRepository.findAll();
    }

    public Department get(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }

    @Transactional
    public Department setQuota(Long deptId, Long limitMb, Long actorId) {
        if (limitMb == null || limitMb < 0) {
            throw new IllegalArgumentException("Quota must be a non-negative number");
        }
        Department d = get(deptId);

        long used = d.getStorageUsedMb() != null ? d.getStorageUsedMb() : 0L;
        if (limitMb < used) {
            throw new IllegalArgumentException(
                    "New quota (" + limitMb + " MB) is below current usage (" + used + " MB)");
        }

        Long oldLimit = d.getStorageLimitMb();
        d.setStorageLimitMb(limitMb);
        Department saved = departmentRepository.save(d);

        auditLogService.log("UPDATE_STORAGE_QUOTA", actorId, null,
                "Department '" + d.getName() + "' quota changed from " + oldLimit + " MB to " + limitMb + " MB");
        return saved;
    }

    @Transactional
    public Department addUsage(Long deptId, long deltaMb) {
        Department d = get(deptId);
        long used = d.getStorageUsedMb() != null ? d.getStorageUsedMb() : 0L;
        long limit = d.getStorageLimitMb() != null ? d.getStorageLimitMb() : Long.MAX_VALUE;

        if (used + deltaMb > limit) {
            throw new IllegalArgumentException(
                    "Storage quota exceeded for department '" + d.getName() + "'");
        }
        d.setStorageUsedMb(used + deltaMb);
        return departmentRepository.save(d);
    }
}
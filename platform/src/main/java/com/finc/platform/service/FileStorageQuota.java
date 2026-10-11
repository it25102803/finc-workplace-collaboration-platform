package com.finc.platform.service;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.Folder;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;

import org.springframework.stereotype.Service;

// Charges file sizes to the folder's department quota, or the uploader's department if the folder has none.
@Service
public class FileStorageQuota {

    private static final long BYTES_PER_MB = 1024L * 1024L;

    private final DepartmentQuotaService departmentQuotaService;
    private final UserRepository userRepository;

    public FileStorageQuota(DepartmentQuotaService departmentQuotaService, UserRepository userRepository) {
        this.departmentQuotaService = departmentQuotaService;
        this.userRepository = userRepository;
    }

    public void charge(Folder folder, Long uploaderId, long sizeBytes) {
        Department department = departmentFor(folder, uploaderId);
        if (department != null) {
            departmentQuotaService.addUsage(department.getDepartmentId(), toMb(sizeBytes));
        }
    }

    public void release(Folder folder, Long uploaderId, long sizeMb) {
        Department department = departmentFor(folder, uploaderId);
        if (department == null) {
            return;
        }
        long used = department.getStorageUsedMb() == null ? 0 : department.getStorageUsedMb();
        // Files uploaded before quotas existed were never charged, so never release more than is recorded.
        long amount = Math.min(sizeMb, used);
        if (amount > 0) {
            departmentQuotaService.addUsage(department.getDepartmentId(), -amount);
        }
    }

    public static long toMb(long sizeBytes) {
        return Math.max(1, (sizeBytes + BYTES_PER_MB - 1) / BYTES_PER_MB);
    }

    private Department departmentFor(Folder folder, Long uploaderId) {
        if (folder != null && folder.getDepartment() != null) {
            return folder.getDepartment();
        }
        if (uploaderId == null) {
            return null;
        }
        return userRepository.findById(uploaderId).map(User::getDepartment).orElse(null);
    }
}

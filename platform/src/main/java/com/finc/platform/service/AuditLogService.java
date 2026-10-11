package com.finc.platform.service;

import com.finc.platform.entity.AuditLog;
import com.finc.platform.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String event, Long actorId, Long targetId, String details) {
        auditLogRepository.save(new AuditLog(event, actorId, targetId, details));
    }

    public List<AuditLog> all() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
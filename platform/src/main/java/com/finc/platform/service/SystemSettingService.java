package com.finc.platform.service;

import com.finc.platform.entity.SystemSetting;
import com.finc.platform.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    @Autowired private SystemSettingRepository repo;
    @Autowired private AuditLogService auditLogService;

    // Default settings seeded on first startup (FR-UM-22)
    private static final Map<String, String[]> DEFAULTS = Map.of(
            "organization.name",       new String[]{"Finc Academy and Consultants (Pvt) Ltd", "Organisation display name"},
            "upload.maxSizeMb",        new String[]{"10", "Maximum upload size per file (MB)"},
            "password.minLength",      new String[]{"6",  "Minimum password length"},
            "session.timeoutMinutes",  new String[]{"30", "Session timeout (minutes)"}
    );

    @PostConstruct
    @Transactional
    public void seedDefaults() {
        DEFAULTS.forEach((key, meta) -> {
            if (repo.findById(key).isEmpty()) {
                repo.save(new SystemSetting(key, meta[0], meta[1]));
            }
        });
    }

    public List<SystemSetting> all() {
        return repo.findAllByOrderByKeyAsc();
    }

    public String get(String key, String fallback) {
        return repo.findById(key).map(SystemSetting::getValue).orElse(fallback);
    }

    public int getInt(String key, int fallback) {
        try { return Integer.parseInt(get(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    @Transactional
    public SystemSetting update(String key, String value, Long actorId) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Setting key required");
        }
        if (value == null) {
            throw new IllegalArgumentException("Setting value required");
        }
        // Basic validation for numeric settings
        if (key.equals("upload.maxSizeMb") || key.equals("password.minLength")
                || key.equals("session.timeoutMinutes")) {
            try { Integer.parseInt(value); }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Value for " + key + " must be a number");
            }
        }

        SystemSetting s = repo.findById(key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown setting: " + key));
        String old = s.getValue();
        s.setValue(value);
        s.setUpdatedBy(actorId);
        SystemSetting saved = repo.save(s);

        auditLogService.log("UPDATE_SETTING", actorId, null,
                "Setting " + key + " changed from '" + old + "' to '" + value + "'");
        return saved;
    }

    @Transactional
    public void bulkUpdate(Map<String, String> updates, Long actorId) {
        updates.forEach((k, v) -> update(k, v, actorId));
    }
}
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
        if (key.equals("upload.maxSizeMb") || key.equals("password.minLength")
                || key.equals("session.timeoutMinutes")) {
            int number;
            try { number = Integer.parseInt(value.trim()); }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException("Value for " + key + " must be a number");
            }
            // Allowed ranges. Uploads can't go above 10 MB because the server itself
            // rejects bigger requests (spring.servlet.multipart.max-file-size).
            int min = key.equals("password.minLength") ? 6 : key.equals("upload.maxSizeMb") ? 1 : 5;
            int max = key.equals("password.minLength") ? 64 : key.equals("upload.maxSizeMb") ? 10 : 1440;
            if (number < min || number > max) {
                throw new IllegalArgumentException("Value for " + key + " must be between " + min + " and " + max);
            }
            value = String.valueOf(number);
        }
        if (key.equals("organization.name") && value.isBlank()) {
            throw new IllegalArgumentException("Organisation name can't be empty");
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

    /**
     * Enforces the admin-configured minimum password length.
     * Called from every password entry point (register, admin create/reset,
     * self-service change, forgot-password reset).
     */
    public void validatePasswordPolicy(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        int minLength = getInt("password.minLength", 6);
        if (rawPassword.length() < minLength) {
            throw new IllegalArgumentException(
                    "Password must be at least " + minLength + " characters long.");
        }
    }
}
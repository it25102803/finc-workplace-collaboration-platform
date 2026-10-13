package com.finc.platform.controller;

import com.finc.platform.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public read-only endpoint for whitelisted system settings.
 * Used by the register page (unauthenticated) to read
 * password.minLength and other safe values.
 */
@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "*")
public class PublicSettingsController {

    @Autowired
    private SystemSettingRepository repo;

    // Only these keys are exposed publicly. Everything else stays admin-only.
    private static final List<String> PUBLIC_KEYS = List.of(
            "password.minLength",
            "organization.name",
            "upload.maxSizeMb"
    );

    @GetMapping("/settings/{key}")
    public ResponseEntity<?> getPublicSetting(@PathVariable String key) {
        if (!PUBLIC_KEYS.contains(key)) {
            return ResponseEntity.notFound().build();
        }
        return repo.findById(key)
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(Map.of("value", s.getValue())))
                .orElse(ResponseEntity.notFound().build());
    }
}
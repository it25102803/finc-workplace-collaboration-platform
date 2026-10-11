package com.finc.platform.service;

import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ProfilePictureService {

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogService auditLogService;

    @Value("${app.upload.dir:uploads/profile-pictures}")
    private String uploadDir;

    @Transactional
    public String upload(Long userId, MultipartFile file, Long actorId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Profile picture must be 2 MB or smaller");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, PNG, GIF or WEBP images are allowed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Save file
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);

        String ext = ".png";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }
        String filename = "user-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
        Path target = dir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = "/" + uploadDir.replace("\\", "/") + "/" + filename;
        user.setProfilePictureUrl(url);
        userRepository.save(user);

        auditLogService.log("UPDATE_PROFILE_PICTURE", actorId, userId,
                "Profile picture updated for " + user.getUsername());

        return url;
    }
}
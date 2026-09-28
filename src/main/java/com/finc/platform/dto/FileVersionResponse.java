package com.finc.platform.dto;

import java.time.LocalDateTime;
import com.finc.platform.entity.FileVersion;

public record FileVersionResponse(Long versionId, int versionNumber, Long fileSizeBytes, String changeNote,
                                  LocalDateTime createdAt, Long fileId, Long createdById) {

    public static FileVersionResponse from(FileVersion v) {
        return new FileVersionResponse(
                v.getVersionId(), v.getVersionNumber(), v.getFileSizeBytes(), v.getChangeNote(),
                v.getCreatedAt(), v.getFile().getFileId(), v.getCreatedBy().getId());
    }
}

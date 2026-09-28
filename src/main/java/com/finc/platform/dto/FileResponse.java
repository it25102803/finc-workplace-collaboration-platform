package com.finc.platform.dto;
import java.time.LocalDateTime;

import com.finc.platform.entity.File;
import com.finc.platform.entity.FileType;

public record FileResponse(Long fileId, String fileName, String mimeType, FileType fileType,
                           Long fileSizeBytes, int currentVersion, boolean isDeleted,
                           LocalDateTime uploadedAt, Long folderId, Long uploadedById) {

    public static FileResponse from(File f) {
        return new FileResponse(
                f.getFileId(), f.getFileName(), f.getMimeType(), f.getFileType(),
                f.getFileSizeBytes(), f.getCurrentVersion(), f.isDeleted(), f.getUploadedAt(),
                f.getFolder().getFolderId(), f.getUploadedBy().getId());
    }
}

package com.finc.platform.dto;
import java.time.LocalDateTime;
import com.finc.platform.entity.Folder;

public record FolderResponse(Long folderId, String name, String path, boolean isShared,
                             LocalDateTime createdAt, Long ownerId, Long departmentId, Long parentFolderId) {

    public static FolderResponse from(Folder f) {
        return new FolderResponse(
                f.getFolderId(), f.getName(), f.getPath(), f.isShared(), f.getCreatedAt(),
                f.getOwner().getId(),
                f.getDepartment() != null ? f.getDepartment().getDepartmentId() : null,
                f.getParentFolder() != null ? f.getParentFolder().getFolderId() : null);
    }
}

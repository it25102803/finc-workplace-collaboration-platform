package com.finc.platform.repository;
import java.util.List;

import com.finc.platform.entity.Folder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository {
    List<Folder> findByParentFolderIsNullAndOwner_UserId(Long ownerId);
    List<Folder> findByParentFolder_FolderId(Long parentFolderId);
    List<Folder> findByDepartment_DepartmentId(Long departmentId);
}

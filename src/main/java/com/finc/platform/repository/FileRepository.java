package com.finc.platform.repository;

import java.util.List;

import com.finc.platform.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByFolder_FolderIdAndIsDeletedFalse(Long folderId);
    List<File> findByFileNameContainingIgnoreCaseAndIsDeletedFalse(String query);
}

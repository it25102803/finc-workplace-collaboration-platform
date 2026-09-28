package com.finc.platform.service;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.entity.File;
import com.finc.platform.entity.FileType;
import com.finc.platform.entity.FileVersion;
import com.finc.platform.entity.Folder;
import com.finc.platform.entity.User;
import com.finc.platform.repository.FileRepository;
import com.finc.platform.repository.FileVersionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service

public class FileService {

    private final FileRepository fileRepository;
    private final FileVersionRepository fileVersionRepository;
    private final FolderService folderService;
    private final StorageService storageService;

    public FileService(FileRepository fileRepository,
                       FileVersionRepository fileVersionRepository,
                       FolderService folderService,
                       StorageService storageService) {
        this.fileRepository = fileRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.folderService = folderService;
        this.storageService = storageService;
    }

    @Transactional
    public File upload(MultipartFile data, Long folderId, User uploadedBy, FileType fileType) {
        Folder folder = folderService.getOrThrow(folderId);

        File file = new File(data.getOriginalFilename(), data.getContentType(), fileType, folder, uploadedBy);
        file.setFileSizeBytes(data.getSize());
        file = fileRepository.save(file);

        String storagePath = storageService.store(data, data.getOriginalFilename());
        file.setStoredName(Path.of(storagePath).getFileName().toString());
        file.setStoragePath(storagePath);

        FileVersion firstVersion = new FileVersion(file, 1, storagePath, data.getSize(), "Initial upload", uploadedBy);
        file.getVersions().add(firstVersion);
        file.setCurrentVersion(1);

        return fileRepository.save(file);
    }

    @Transactional(readOnly = true)
    public byte[] download(Long fileId) {
        File file = getOrThrow(fileId);
        return storageService.read(file.getStoragePath());
    }

    public String preview(Long fileId) {
        File file = getOrThrow(fileId);
        return "Preview unavailable for " + file.getMimeType() + " — download to view: " + file.getFileName();
    }

    @Transactional
    public File rename(Long fileId, String name) {
        File file = getOrThrow(fileId);
        file.rename(name);
        return file;
    }

    @Transactional
    public File move(Long fileId, Long targetFolderId) {
        File file = getOrThrow(fileId);
        Folder target = folderService.getOrThrow(targetFolderId);
        file.move(target);
        return file;
    }

    @Transactional
    public FileVersion createVersion(Long fileId, MultipartFile data, User createdBy, String changeNote) {
        File file = getOrThrow(fileId);
        String storagePath = storageService.store(data, file.getFileName());
        // Not currentVersion + 1: after restoring an old version, currentVersion goes backwards.
        int nextVersionNumber = fileVersionRepository.findByFile_FileIdOrderByVersionNumberDesc(fileId)
                .stream().findFirst().map(v -> v.getVersionNumber() + 1).orElse(1);

        FileVersion version = new FileVersion(file, nextVersionNumber, storagePath, data.getSize(), changeNote, createdBy);
        fileVersionRepository.save(version);

        file.setCurrentVersion(nextVersionNumber);
        file.setStoragePath(storagePath);
        file.setFileSizeBytes(data.getSize());

        return version;
    }

    @Transactional(readOnly = true)
    public List<FileVersion> getVersionHistory(Long fileId) {
        return fileVersionRepository.findByFile_FileIdOrderByVersionNumberDesc(fileId);
    }

    @Transactional
    public File restoreVersion(Long versionId) {
        FileVersion version = fileVersionRepository.findById(versionId)
                .orElseThrow(() -> new NoSuchElementException("Version not found: " + versionId));
        File file = version.getFile();
        file.setStoragePath(version.getStoragePath());
        file.setFileSizeBytes(version.getFileSizeBytes());
        file.setCurrentVersion(version.getVersionNumber());
        return file;
    }

    @Transactional
    public void delete(Long fileId) {
        File file = getOrThrow(fileId);
        file.markDeleted();
    }

    @Transactional(readOnly = true)
    public File getOrThrow(Long fileId) {
        return fileRepository.findById(fileId)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new NoSuchElementException("File not found: " + fileId));
    }
}

package com.finc.platform.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.finc.platform.dto.FileResponse;
import com.finc.platform.dto.FileVersionResponse;
import com.finc.platform.dto.RenameRequest;
import com.finc.platform.entity.File;
import com.finc.platform.entity.FileType;
import com.finc.platform.entity.User;
import com.finc.platform.service.FileService;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")

public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(@RequestParam MultipartFile data,
                                               @RequestParam Long folderId,
                                               @RequestParam Long uploaderId,
                                               @RequestParam FileType fileType) {
        User uploader = new User();
        uploader.setId(uploaderId);
        return ResponseEntity.ok(FileResponse.from(fileService.upload(data, folderId, uploader, fileType)));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> get(@PathVariable Long fileId) {
        return ResponseEntity.ok(FileResponse.from(fileService.getOrThrow(fileId)));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        File file = fileService.getOrThrow(fileId);
        byte[] bytes = fileService.download(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.getFileName(), StandardCharsets.UTF_8).build().toString())
                .body(bytes);
    }

    @GetMapping("/{fileId}/preview")
    public ResponseEntity<String> preview(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.preview(fileId));
    }

    @PutMapping("/{fileId}/rename")
    public ResponseEntity<FileResponse> rename(@PathVariable Long fileId, @RequestBody RenameRequest request) {
        return ResponseEntity.ok(FileResponse.from(fileService.rename(fileId, request.name())));
    }

    @PutMapping("/{fileId}/move")
    public ResponseEntity<FileResponse> move(@PathVariable Long fileId, @RequestParam Long targetFolderId) {
        return ResponseEntity.ok(FileResponse.from(fileService.move(fileId, targetFolderId)));
    }

    @PostMapping("/{fileId}/versions")
    public ResponseEntity<FileVersionResponse> createVersion(@PathVariable Long fileId,
                                                             @RequestParam MultipartFile data,
                                                             @RequestParam Long createdById,
                                                             @RequestParam(required = false) String changeNote) {
        User createdBy = new User();
        createdBy.setId(createdById);
        return ResponseEntity.ok(FileVersionResponse.from(
                fileService.createVersion(fileId, data, createdBy, changeNote)));
    }

    @GetMapping("/{fileId}/versions")
    public ResponseEntity<List<FileVersionResponse>> getVersionHistory(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.getVersionHistory(fileId).stream()
                .map(FileVersionResponse::from).toList());
    }

    @PostMapping("/versions/{versionId}/restore")
    public ResponseEntity<FileResponse> restoreVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(FileResponse.from(fileService.restoreVersion(versionId)));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}

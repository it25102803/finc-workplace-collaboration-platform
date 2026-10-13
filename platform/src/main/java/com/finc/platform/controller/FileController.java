package com.finc.platform.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
                                               @RequestParam(required = false) Long uploaderId,
                                               @RequestParam FileType fileType,
                                               @RequestParam(required = false) Long actorId) {
        // The uploader is the logged-in user (actorId from the login token), not whatever the page sends
        User uploader = new User();
        uploader.setId(actorId != null ? actorId : uploaderId);
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
    public ResponseEntity<byte[]> preview(@PathVariable Long fileId) {
        File file = fileService.getOrThrow(fileId);
        if (!fileService.isPreviewable(file)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Preview is not available for this file type. Download it to open it.".getBytes(StandardCharsets.UTF_8));
        }
        String mimeType = file.getMimeType().toLowerCase();
        // Text is sent as plain text so an uploaded .html file can never run as a web page.
        MediaType type = mimeType.startsWith("text/")
                ? new MediaType("text", "plain", StandardCharsets.UTF_8)
                : MediaType.parseMediaType(mimeType);
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.getFileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(fileService.download(fileId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FileResponse>> listUploadedBy(@PathVariable Long userId,
                                                             @RequestParam(required = false) Long actorId) {
        if (actorId == null || !actorId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(fileService.listUploadedBy(userId).stream().map(FileResponse::from).toList());
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
                                                             @RequestParam(required = false) Long createdById,
                                                             @RequestParam(required = false) String changeNote,
                                                             @RequestParam(required = false) Long actorId) {
        User createdBy = new User();
        createdBy.setId(actorId != null ? actorId : createdById);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}

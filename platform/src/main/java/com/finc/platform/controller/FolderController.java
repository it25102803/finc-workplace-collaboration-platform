package com.finc.platform.controller;
import java.util.List;

import com.finc.platform.dto.*;
import com.finc.platform.entity.User;
import com.finc.platform.service.FolderService;

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

@RestController
@RequestMapping("/api/folders")

public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<FolderResponse> createRootFolder(@RequestBody CreateFolderRequest request) {
        return ResponseEntity.ok(FolderResponse.from(
                folderService.createRootFolder(request.name(), request.ownerId(), request.departmentId())));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> get(@PathVariable Long folderId) {
        return ResponseEntity.ok(FolderResponse.from(folderService.getOrThrow(folderId)));
    }

    @PostMapping("/{folderId}/subfolders")
    public ResponseEntity<FolderResponse> createSubFolder(@PathVariable Long folderId,
                                                          @RequestBody CreateSubFolderRequest request) {
        // TODO: once Spring Security is wired up, resolve the owner from the
        // authenticated principal instead of trusting a raw ownerId from the client.
        User owner = new User();
        owner.setId(request.ownerId());
        return ResponseEntity.ok(FolderResponse.from(
                folderService.createSubFolder(folderId, request.name(), owner)));
    }

    @PutMapping("/{folderId}/rename")
    public ResponseEntity<FolderResponse> rename(@PathVariable Long folderId, @RequestBody RenameRequest request) {
        return ResponseEntity.ok(FolderResponse.from(folderService.rename(folderId, request.name())));
    }

    @PutMapping("/{folderId}/move")
    public ResponseEntity<FolderResponse> move(@PathVariable Long folderId, @RequestParam Long targetFolderId) {
        return ResponseEntity.ok(FolderResponse.from(folderService.move(folderId, targetFolderId)));
    }

    @GetMapping("/{folderId}/contents")
    public ResponseEntity<List<FileResponse>> listContents(@PathVariable Long folderId) {
        return ResponseEntity.ok(folderService.listContents(folderId).stream().map(FileResponse::from).toList());
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable Long folderId) {
        folderService.delete(folderId);
        return ResponseEntity.noContent().build();
    }
}

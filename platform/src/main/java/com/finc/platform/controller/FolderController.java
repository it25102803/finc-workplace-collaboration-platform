package com.finc.platform.controller;
import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.dto.*;
import com.finc.platform.entity.User;
import com.finc.platform.entity.Folder;
import com.finc.platform.service.AccessRules;
import com.finc.platform.service.FileAccess;
import com.finc.platform.service.FolderService;

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

@RestController
@RequestMapping("/api/folders")

public class FolderController {

    // Folders are private to their owner (a system admin can manage any folder).
    private final FolderService folderService;
    private final FileAccess fileAccess;

    public FolderController(FolderService folderService, FileAccess fileAccess) {
        this.folderService = folderService;
        this.fileAccess = fileAccess;
    }

    @GetMapping("/roots")
    public ResponseEntity<List<FolderResponse>> listRoots(@RequestParam Long ownerId,
                                                          @RequestParam(required = false) Long actorId) {
        User actor = fileAccess.actor(actorId);
        if (!actor.getId().equals(ownerId) && !AccessRules.isSystemAdmin(actor)) {
            throw new SecurityException("You can only list your own folders");
        }
        return ResponseEntity.ok(folderService.listRootFolders(ownerId).stream()
                .map(FolderResponse::from).toList());
    }

    @GetMapping("/{folderId}/subfolders")
    public ResponseEntity<List<FolderResponse>> listSubfolders(@PathVariable Long folderId,
                                                               @RequestParam(required = false) Long actorId) {
        owned(folderId, actorId);
        return ResponseEntity.ok(folderService.listSubFolders(folderId).stream()
                .map(FolderResponse::from).toList());
    }
    @PostMapping
    public ResponseEntity<FolderResponse> createRootFolder(@RequestBody CreateFolderRequest request,
                                                           @RequestParam(required = false) Long actorId) {
        // The owner is the logged-in user (actorId from the login token)
        Long ownerId = actorId != null ? actorId : request.ownerId();
        return ResponseEntity.ok(FolderResponse.from(
                folderService.createRootFolder(request.name(), ownerId, request.departmentId())));
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> get(@PathVariable Long folderId,
                                              @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(FolderResponse.from(owned(folderId, actorId)));
    }

    @PostMapping("/{folderId}/subfolders")
    public ResponseEntity<FolderResponse> createSubFolder(@PathVariable Long folderId,
                                                          @RequestBody CreateSubFolderRequest request,
                                                          @RequestParam(required = false) Long actorId) {
        // A sub-folder belongs to the same person as the folder it is created in
        Folder parent = owned(folderId, actorId);
        User owner = parent.getOwner();
        return ResponseEntity.ok(FolderResponse.from(
                folderService.createSubFolder(folderId, request.name(), owner)));
    }

    @PutMapping("/{folderId}/rename")
    public ResponseEntity<FolderResponse> rename(@PathVariable Long folderId, @RequestBody RenameRequest request,
                                                 @RequestParam(required = false) Long actorId) {
        owned(folderId, actorId);
        return ResponseEntity.ok(FolderResponse.from(folderService.rename(folderId, request.name())));
    }

    @PutMapping("/{folderId}/move")
    public ResponseEntity<FolderResponse> move(@PathVariable Long folderId, @RequestParam Long targetFolderId,
                                               @RequestParam(required = false) Long actorId) {
        owned(folderId, actorId);
        owned(targetFolderId, actorId);
        return ResponseEntity.ok(FolderResponse.from(folderService.move(folderId, targetFolderId)));
    }

    @GetMapping("/{folderId}/contents")
    public ResponseEntity<List<FileResponse>> listContents(@PathVariable Long folderId,
                                                           @RequestParam(required = false) Long actorId) {
        owned(folderId, actorId);
        return ResponseEntity.ok(folderService.listContents(folderId).stream().map(FileResponse::from).toList());
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable Long folderId,
                                       @RequestParam(required = false) Long actorId) {
        owned(folderId, actorId);
        folderService.delete(folderId);
        return ResponseEntity.noContent().build();
    }

    private Folder owned(Long folderId, Long actorId) {
        Folder folder = folderService.getOrThrow(folderId);
        fileAccess.requireManageFolder(folder, fileAccess.actor(actorId));
        return folder;
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
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

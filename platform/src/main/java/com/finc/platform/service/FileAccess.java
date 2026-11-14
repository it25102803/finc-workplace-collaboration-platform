package com.finc.platform.service;

import com.finc.platform.entity.ArticleStatus;
import com.finc.platform.entity.File;
import com.finc.platform.entity.Folder;
import com.finc.platform.entity.RoleType;
import com.finc.platform.entity.User;
import com.finc.platform.repository.TaskAttachmentRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Who may open or change files and folders.
//  - Folders belong to their owner (a system admin can manage any folder).
//  - A file can be changed by its folder's owner, its uploader, or a system admin.
//  - A file can also be read when it is attached to a published Knowledge Base article,
//    to any article (for KB editors), or to a task the reader is allowed to see.
@Service
public class FileAccess {

    private final UserRepository userRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskService taskService;

    public FileAccess(UserRepository userRepository,
                      TaskAttachmentRepository taskAttachmentRepository,
                      TaskService taskService) {
        this.userRepository = userRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.taskService = taskService;
    }

    public User actor(Long actorId) {
        if (actorId == null) {
            throw new SecurityException("Login required");
        }
        return userRepository.findById(actorId)
                .orElseThrow(() -> new SecurityException("Unknown user"));
    }

    public boolean canManageFolder(Folder folder, User user) {
        return AccessRules.isSystemAdmin(user) || AccessRules.isSameUser(user, folder.getOwner());
    }

    public boolean canManageFile(File file, User user) {
        return canManageFolder(file.getFolder(), user) || AccessRules.isSameUser(user, file.getUploadedBy());
    }

    @Transactional(readOnly = true)
    public boolean canReadFile(File file, User user) {
        if (canManageFile(file, user)) {
            return true;
        }
        RoleType role = AccessRules.roleOf(user);
        boolean kbEditor = role == RoleType.ROLE_SYSTEM_ADMIN || role == RoleType.ROLE_DEPT_ADMIN;
        boolean onArticle = file.getArticles().stream()
                .anyMatch(article -> article.getStatus() == ArticleStatus.PUBLISHED || kbEditor);
        if (onArticle) {
            return true;
        }
        return taskAttachmentRepository.findByFile_FileId(file.getFileId()).stream()
                .anyMatch(attachment -> taskService.canView(attachment.getTask(), user));
    }

    public void requireManageFolder(Folder folder, User user) {
        if (!canManageFolder(folder, user)) {
            throw new SecurityException("You can only change your own folders");
        }
    }

    public void requireManageFile(File file, User user) {
        if (!canManageFile(file, user)) {
            throw new SecurityException("You can only change your own files");
        }
    }

    public void requireReadFile(File file, User user) {
        if (!canReadFile(file, user)) {
            throw new SecurityException("You don't have access to this file");
        }
    }
}

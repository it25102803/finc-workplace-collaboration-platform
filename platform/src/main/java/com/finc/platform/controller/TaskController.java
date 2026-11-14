package com.finc.platform.controller;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskAttachment;
import com.finc.platform.entity.TaskComment;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.entity.User;
import com.finc.platform.repository.UserRepository;
import com.finc.platform.service.FileAccess;
import com.finc.platform.service.FileService;
import com.finc.platform.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

// actorId is the logged-in user, filled in from the login token by RoleGuardFilter.
// Creating, editing and deleting tasks is limited to team leaders and admins by RoleGuardFilter;
// status, progress, comments and attachments are checked here (assignee, creator or manager).
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;
    private final FileAccess fileAccess;
    private final FileService fileService;

    public TaskController(TaskService taskService, UserRepository userRepository,
                          FileAccess fileAccess, FileService fileService) {
        this.taskService = taskService;
        this.userRepository = userRepository;
        this.fileAccess = fileAccess;
        this.fileService = fileService;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(taskService.getTasksForUserView(actorId));
    }

    // Only the people this user is allowed to assign tasks to
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAssignableUsers(@RequestParam(required = false) Long actorId) {
        User actor = actor(actorId);
        List<Map<String, Object>> list = userRepository.findAll().stream()
                .filter(u -> taskService.canAssign(actor, u))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    String displayName = u.getFirstName() != null
                            ? u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "")
                            : u.getUsername();
                    map.put("name", displayName.trim());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id,
                                            @RequestParam(required = false) Long actorId) {
        return ResponseEntity.ok(requireView(id, actorId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable Long userId,
                                                     @RequestParam(required = false) Long actorId) {
        User actor = actor(actorId);
        if (!actor.getId().equals(userId) && !taskService.isManager(actor)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(taskService.getTasksByUser(userId).stream()
                .filter(task -> taskService.canView(task, actor)).toList());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable TaskStatus status,
                                                       @RequestParam(required = false) Long actorId) {
        User actor = actor(actorId);
        return ResponseEntity.ok(taskService.getTasksByStatus(status).stream()
                .filter(task -> taskService.canView(task, actor)).toList());
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks(@RequestParam(required = false) Long actorId) {
        User actor = actor(actorId);
        return ResponseEntity.ok(taskService.getOverdueTasks().stream()
                .filter(task -> taskService.canView(task, actor)).toList());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
        requireCanAssign(actor(actorId), assignedUserId);
        Task created = taskService.createTask(task, actorId, assignedUserId, eventId);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
        Task existing = taskService.getTaskById(id);
        User actor = actor(actorId);
        if (!taskService.canManage(existing, actor)) {
            throw new SecurityException("Only the person who created this task (or an administrator) can edit it");
        }
        Long currentAssignee = existing.getAssignee() == null ? null : existing.getAssignee().getId();
        boolean reassigning = !java.util.Objects.equals(currentAssignee, normalise(assignedUserId));
        if (reassigning) {
            requireCanAssign(actor, assignedUserId);
        }
        Task updated = taskService.updateTask(id, task, actorId, assignedUserId, eventId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateStatus(@PathVariable Long id,
                                             @RequestParam TaskStatus status,
                                             @RequestParam(required = false) Long actorId) {
        requireWorkOn(id, actorId);
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Task> updateProgress(@PathVariable Long id,
                                               @RequestParam Integer progress,
                                               @RequestParam(required = false) Long actorId) {
        requireWorkOn(id, actorId);
        return ResponseEntity.ok(taskService.updateProgress(id, progress));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id,
                                           @RequestParam(required = false) Long actorId) {
        if (!taskService.canManage(taskService.getTaskById(id), actor(actorId))) {
            throw new SecurityException("Only the person who created this task (or an administrator) can delete it");
        }
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TaskComment> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestParam(required = false) Long actorId
    ) {
        requireWorkOn(id, actorId);
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        return ResponseEntity.ok(taskService.addComment(id, content.trim(), actorId));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TaskComment>> getComments(@PathVariable Long id,
                                                         @RequestParam(required = false) Long actorId) {
        requireView(id, actorId);
        return ResponseEntity.ok(taskService.getComments(id));
    }

    // Attach a file that is already in the File Manager (FRD: "Attach files to tasks").
    @PostMapping("/{id}/attachments")
    public ResponseEntity<Map<String, Object>> addAttachment(@PathVariable Long id,
                                                             @RequestParam Long fileId,
                                                             @RequestParam(required = false) Long actorId) {
        requireWorkOn(id, actorId);
        // You can only attach files you are allowed to open yourself
        fileAccess.requireReadFile(fileService.getOrThrow(fileId), actor(actorId));
        return ResponseEntity.ok(toView(taskService.addAttachment(id, fileId)));
    }

    @GetMapping("/{id}/attachments")
    public ResponseEntity<List<Map<String, Object>>> getAttachments(@PathVariable Long id,
                                                                     @RequestParam(required = false) Long actorId) {
        requireView(id, actorId);
        return ResponseEntity.ok(taskService.getAttachments(id).stream().map(this::toView).toList());
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<Void> removeAttachment(@PathVariable Long id,
                                                 @PathVariable Long attachmentId,
                                                 @RequestParam(required = false) Long actorId) {
        requireWorkOn(id, actorId);
        taskService.removeAttachment(id, attachmentId);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toView(TaskAttachment attachment) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("attachmentId", attachment.getAttachmentId());
        view.put("fileId", attachment.getFile() != null ? attachment.getFile().getFileId() : null);
        view.put("fileName", attachment.getFileName());
        view.put("fileSize", attachment.getFileSize());
        view.put("uploadedAt", attachment.getUploadedAt());
        return view;
    }

    private User actor(Long actorId) {
        if (actorId == null) {
            throw new SecurityException("Login required");
        }
        return userRepository.findById(actorId)
                .orElseThrow(() -> new SecurityException("Unknown user"));
    }

    private Task requireView(Long taskId, Long actorId) {
        Task task = taskService.getTaskById(taskId);
        if (!taskService.canView(task, actor(actorId))) {
            throw new SecurityException("You don't have access to this task");
        }
        return task;
    }

    private static Long normalise(Long userId) {
        return userId == null || userId <= 0 ? null : userId;
    }

    private void requireCanAssign(User actor, Long assignedUserId) {
        Long targetId = normalise(assignedUserId);
        User target = null;
        if (targetId != null) {
            target = userRepository.findById(targetId)
                    .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
        }
        if (!taskService.canAssign(actor, target)) {
            throw new SecurityException("You can't assign tasks to this user. Team leaders assign employees "
                    + "in their own department; department admins assign team leaders and employees in theirs.");
        }
    }

    private void requireWorkOn(Long taskId, Long actorId) {
        Task task = taskService.getTaskById(taskId);
        if (!taskService.canWorkOn(task, actor(actorId))) {
            throw new SecurityException("Only the assignee, the creator or a team leader can update this task");
        }
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> forbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
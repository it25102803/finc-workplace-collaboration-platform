package com.finc.platform.controller;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskComment;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.repository.UserRepository;
import com.finc.platform.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(@RequestParam(required = false) Long currentUserId) {
        return ResponseEntity.ok(taskService.getTasksForUserView(currentUserId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAssignableUsers() {
        List<Map<String, Object>> list = userRepository.findAll().stream().map(u -> {
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
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTasksByUser(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        return ResponseEntity.ok(taskService.getOverdueTasks());
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestParam(required = false, defaultValue = "1") Long createdByUserId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
        Task created = taskService.createTask(task, createdByUserId, assignedUserId, eventId);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @RequestParam(required = false, defaultValue = "1") Long createdByUserId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
        Task updated = taskService.updateTask(id, task, createdByUserId, assignedUserId, eventId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Task> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        return ResponseEntity.ok(taskService.updateProgress(id, progress));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TaskComment> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @RequestParam(required = false, defaultValue = "1") Long userId
    ) {
        return ResponseEntity.ok(taskService.addComment(id, body.get("content"), userId));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<TaskComment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getComments(id));
    }
}
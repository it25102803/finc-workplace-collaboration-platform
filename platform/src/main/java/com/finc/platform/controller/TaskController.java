package com.finc.platform.controller;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskComment;
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
<<<<<<< HEAD
    public ResponseEntity<List<Task>> getAllTasks(@RequestParam(required = false) Long currentUserId) {
        return ResponseEntity.ok(taskService.getTasksForUserView(currentUserId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAssignableUsers() {
        List<Map<String, Object>> list = userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            String displayName = u.getFirstName() != null ? u.getFirstName() + " " + (u.getLastName() != null ? u.getLastName() : "") : u.getUsername();
            map.put("name", displayName.trim());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
=======
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
    }

    @GetMapping("/{id}")
<<<<<<< HEAD
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
=======
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
    }

    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestParam(required = false, defaultValue = "1") Long createdByUserId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
<<<<<<< HEAD
        Task created = taskService.createTask(task, createdByUserId, assignedUserId, eventId);
        return ResponseEntity.ok(created);
=======
        return ResponseEntity.ok(
                taskService.createTask(task, assignedUserId)
        );
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @RequestParam(required = false, defaultValue = "1") Long createdByUserId,
            @RequestParam(required = false) Long assignedUserId,
            @RequestParam(required = false) Long eventId
    ) {
<<<<<<< HEAD
        Task updated = taskService.updateTask(id, task, createdByUserId, assignedUserId, eventId);
        return ResponseEntity.ok(updated);
    }

=======
        return ResponseEntity.ok(
                taskService.updateTask(
                        id,
                        task,
                        assignedUserId
                )
        );
    }

    // DELETE TASK
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id
    ) {

        taskService.deleteTask(id);

        return ResponseEntity.ok(
                "Task deleted successfully"
        );
    }

    // UPDATE PROGRESS
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Task> updateProgress(@PathVariable Long id, @RequestParam Integer progress) {
        return ResponseEntity.ok(taskService.updateProgress(id, progress));
    }

<<<<<<< HEAD
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
=======
    // GET TASKS FOR USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                taskService.getTasksByUser(userId)
        );
    }

    // GET TASKS BY STATUS
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable TaskStatus status
    ) {
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9

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
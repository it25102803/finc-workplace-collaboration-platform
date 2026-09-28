package com.finc.platform.controller;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // =========================================================
    // GET ALL TASKS
    // =========================================================
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }

    // =========================================================
    // GET TASKS ASSIGNED TO A SPECIFIC USER
    //
    // Example:
    // GET /api/tasks/user/5
    //
    // This returns ONLY tasks assigned to User ID 5.
    // =========================================================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                taskService.getTasksByUser(userId)
        );
    }

    // =========================================================
    // GET TASK BY ID
    //
    // Example:
    // GET /api/tasks/10
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    // =========================================================
    // CREATE TASK
    //
    // Example:
    // POST /api/tasks?assignedUserId=5
    // =========================================================
    @PostMapping
    public ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestParam(required = false) Long assignedUserId
    ) {

        return ResponseEntity.ok(
                taskService.createTask(
                        task,
                        assignedUserId
                )
        );
    }

    // =========================================================
    // UPDATE TASK
    //
    // Example:
    // PUT /api/tasks/10?assignedUserId=5
    // =========================================================
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @RequestBody Task task,
            @RequestParam(required = false) Long assignedUserId
    ) {

        return ResponseEntity.ok(
                taskService.updateTask(
                        id,
                        task,
                        assignedUserId
                )
        );
    }

    // =========================================================
    // DELETE TASK
    //
    // Example:
    // DELETE /api/tasks/10
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id
    ) {

        taskService.deleteTask(id);

        return ResponseEntity.ok(
                "Task deleted successfully"
        );
    }

    // =========================================================
    // UPDATE TASK PROGRESS
    //
    // Example:
    // PATCH /api/tasks/10/progress?progress=75
    // =========================================================
    @PatchMapping("/{id}/progress")
    public ResponseEntity<Task> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progress
    ) {

        return ResponseEntity.ok(
                taskService.updateProgress(
                        id,
                        progress
                )
        );
    }

    // =========================================================
    // GET TASKS BY STATUS
    //
    // Example:
    // GET /api/tasks/status/IN_PROGRESS
    // =========================================================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(
            @PathVariable TaskStatus status
    ) {

        return ResponseEntity.ok(
                taskService.getTasksByStatus(status)
        );
    }
}
package com.finc.platform.controller;

import com.finc.platform.entity.TaskComment;
import com.finc.platform.service.TaskCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskCommentController {

    private final TaskCommentService commentService;

    public TaskCommentController(TaskCommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskComment>> getComments(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(
                commentService.getCommentsByTask(taskId)
        );
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskComment> createComment(
            @PathVariable Long taskId,
            @RequestParam Long authorId,
            @RequestBody TaskComment comment
    ) {
        return ResponseEntity.ok(
                commentService.createComment(
                        taskId,
                        authorId,
                        comment
                )
        );
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<TaskComment> updateComment(
            @PathVariable Long commentId,
            @RequestParam String content
    ) {
        return ResponseEntity.ok(
                commentService.updateComment(
                        commentId,
                        content
                )
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);

        return ResponseEntity.ok(
                "Comment deleted successfully"
        );
    }
}

package com.finc.platform.service;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskComment;
import com.finc.platform.entity.User;
import com.finc.platform.repository.TaskCommentRepository;
import com.finc.platform.repository.TaskRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskCommentService(
            TaskCommentRepository commentRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskComment> getCommentsByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException(
                    "Task not found with ID: " + taskId);
        }

        return commentRepository
                .findByTask_TaskIdOrderByCreatedAtAsc(taskId);
    }

    public TaskComment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Comment not found with ID: " + commentId));
    }

    @Transactional
    public TaskComment createComment(
            Long taskId,
            Long authorId,
            TaskComment comment
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with ID: " + taskId));

        User author = userRepository.findById(authorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + authorId));

        if (comment.getContent() == null
                || comment.getContent().isBlank()) {
            throw new IllegalArgumentException(
                    "Comment content is required"
            );
        }

        comment.setTask(task);
        comment.setAuthor(author);

        return commentRepository.save(comment);
    }

    @Transactional
    public TaskComment updateComment(
            Long commentId,
            String content
    ) {
        TaskComment comment = getCommentById(commentId);

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment content is required"
            );
        }

        comment.setContent(content);

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException(
                    "Comment not found with ID: " + commentId);
        }

        commentRepository.deleteById(commentId);
    }
}

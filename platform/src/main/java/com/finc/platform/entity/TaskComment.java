package com.finc.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")
public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"comments", "attachments", "hibernateLazyInitializer", "handler"})
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "roles", "department", "hibernateLazyInitializer", "handler"})
    private User author;

    public TaskComment() {}

    public TaskComment(String content, Task task, User author) {
        this.content = content;
        this.task = task;
        this.author = author;
        this.createdAt = LocalDateTime.now();
    }

    public Long getCommentId() { return commentId; }
    public void setCommentId(Long commentId) { this.commentId = commentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}
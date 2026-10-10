package com.finc.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_attachments")
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @JsonIgnoreProperties({"attachments", "comments", "hibernateLazyInitializer", "handler"})
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private File file;

    public TaskAttachment() {}

    public TaskAttachment(String fileName, String filePath, Long fileSize, Task task) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.task = task;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }
}
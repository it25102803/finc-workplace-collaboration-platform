package com.finc.platform.service;

import com.finc.platform.entity.*;
import com.finc.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final FileRepository fileRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            CalendarEventRepository calendarEventRepository,
            TaskCommentRepository taskCommentRepository,
            TaskAttachmentRepository taskAttachmentRepository,
            FileRepository fileRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskAttachmentRepository = taskAttachmentRepository;
        this.fileRepository = fileRepository;
    }

    // Team leaders and administrators manage tasks, so they see every task.
    public boolean isManager(User user) {
        RoleType role = user == null || user.getRole() == null ? null : user.getRole().getRoleType();
        return role == RoleType.ROLE_SYSTEM_ADMIN
                || role == RoleType.ROLE_DEPT_ADMIN
                || role == RoleType.ROLE_TEAM_LEADER;
    }

    // Status, progress, comments and attachments: the assignee, the creator, or a manager.
    public boolean canWorkOn(Task task, User user) {
        if (user == null) return false;
        if (isManager(user)) return true;
        boolean assignee = task.getAssignee() != null && user.getId().equals(task.getAssignee().getId());
        boolean creator = task.getCreator() != null && user.getId().equals(task.getCreator().getId());
        return assignee || creator;
    }
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getTasksForUserView(Long userId) {
        if (userId == null) {
            return List.of();
        }
        User user = userRepository.findById(userId).orElse(null);
        if (isManager(user)) {
            return taskRepository.findAll();
        }
        return taskRepository.findVisibleToUser(userId);
    }

    public List<Task> getTasksByUser(Long userId) {
        return taskRepository.findByAssignedUser_id(userId);
    }

    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    @Transactional
    public Task createTask(Task task, Long createdByUserId, Long assignedUserId, Long eventId) {
        User creator = null;
        if (createdByUserId != null) {
            creator = userRepository.findById(createdByUserId).orElse(null);
        }
        task.setCreatedBy(creator);

        if (assignedUserId != null && assignedUserId > 0) {
            task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
        } else {
            task.setAssignedUser(null);
        }

        if (eventId != null) {
            task.setRelatedEvent(calendarEventRepository.findById(eventId).orElse(null));
        }

        validateTask(task);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
            task.setProgress(100);
        }

        Task savedTask = taskRepository.save(task);

        if (savedTask.getDueDate() != null) {
            syncTaskDeadlineEvent(savedTask);
        }

        return savedTask;
    }

    @Transactional
    public Task updateTask(Long taskId, Task updatedTask, Long createdByUserId, Long assignedUserId, Long eventId) {
        Task existing = getTaskById(taskId);

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(updatedTask.getStatus());
        existing.setPriority(updatedTask.getPriority());
        existing.setProgress(updatedTask.getProgress());
        existing.setStartDate(updatedTask.getStartDate());
        existing.setDueDate(updatedTask.getDueDate());

        if (assignedUserId != null && assignedUserId > 0) {
            existing.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
        } else {
            existing.setAssignedUser(null);
        }

        validateTask(existing);

        if (existing.getStatus() == TaskStatus.COMPLETED) {
            if (existing.getCompletedAt() == null) {
                existing.setCompletedAt(LocalDateTime.now());
            }
            existing.setProgress(100);
        } else {
            existing.setCompletedAt(null);
        }

        Task saved = taskRepository.save(existing);
        syncTaskDeadlineEvent(saved);
        return saved;
    }

    // Employees move their own tasks along the board (To Do -> In Progress -> Review -> Done).
    @Transactional
    public Task updateStatus(Long taskId, TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        Task task = getTaskById(taskId);
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            task.setProgress(100);
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
            if (status == TaskStatus.TODO) {
                task.setProgress(0);
            } else if (task.getProgress() == 100) {
                task.setProgress(90);
            }
        }
        return taskRepository.save(task);
    }

    // Links a file from the File Manager to the task.
    @Transactional
    public TaskAttachment addAttachment(Long taskId, Long fileId) {
        Task task = getTaskById(taskId);
        File file = fileRepository.findById(fileId)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));
        TaskAttachment attachment = new TaskAttachment(file.getFileName(), file.getStoragePath(),
                file.getFileSizeBytes() == null ? 0L : file.getFileSizeBytes(), task);
        attachment.setFile(file);
        return taskAttachmentRepository.save(attachment);
    }

    public List<TaskAttachment> getAttachments(Long taskId) {
        return taskAttachmentRepository.findByTaskTaskId(taskId);
    }

    @Transactional
    public void removeAttachment(Long taskId, Long attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .filter(a -> a.getTask().getTaskId().equals(taskId))
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        taskAttachmentRepository.delete(attachment);
    }

    private void syncTaskDeadlineEvent(Task task) {
        if (task.getDueDate() == null) return;

        String deadlineTitle = "📌 Deadline: " + task.getTitle();

        CalendarEvent event = task.getRelatedEvent();
        if (event == null) {
            event = new CalendarEvent();
            event.setEventType(EventType.DEADLINE);
            event.setVisibility(EventVisibility.PUBLIC);
            event.setRelatedTask(task);
            event.setAllDay(true);
        }
        event.setTitle(deadlineTitle);
        event.setDescription("Task deadline: " + task.getTitle() + " (Priority: " + task.getPriority() + ")");
        event.setEventDate(task.getDueDate());
        event.setStartTime(task.getDueDate().atStartOfDay());
        event.setEndTime(task.getDueDate().atTime(LocalTime.MAX));
        event.setCreatedByUser(task.getCreator());

        CalendarEvent savedEvent = calendarEventRepository.save(event);
        task.setRelatedEvent(savedEvent);
    }

    @Transactional
    public Task updateProgress(Long taskId, Integer progress) {
        Task task = getTaskById(taskId);
        validateProgress(progress);
        task.setProgress(progress);

        if (progress == 100) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        } else if (progress > 0) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setCompletedAt(null);
        } else {
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
        }
        return taskRepository.save(task);
    }

    // The task and its calendar deadline point at each other, so unlink them before deleting both.
    @Transactional
    public void deleteTask(Long taskId) {
        Task task = getTaskById(taskId);
        task.setRelatedEvent(null);
        taskRepository.saveAndFlush(task);
        calendarEventRepository.deleteAll(calendarEventRepository.findByRelatedTask_TaskId(taskId));
        taskRepository.delete(task);
    }

    public TaskComment addComment(Long taskId, String content, Long userId) {
        Task task = getTaskById(taskId);
        User author = (userId != null) ? userRepository.findById(userId).orElse(null) : null;
        TaskComment comment = new TaskComment(content, task, author);
        return taskCommentRepository.save(comment);
    }

    public List<TaskComment> getComments(Long taskId) {
        return taskCommentRepository.findByTaskTaskIdOrderByCreatedAtAsc(taskId);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndStatusNotIn(
                LocalDate.now(),
                List.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED)
        );
    }

    private void validateTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Task title is required");
        }
        if (task.getProgress() == null) task.setProgress(0);
        validateProgress(task.getProgress());
        if (task.getStatus() == null) task.setStatus(TaskStatus.TODO);
        if (task.getPriority() == null) task.setPriority(TaskPriority.MEDIUM);
    }

    private void validateProgress(Integer progress) {
        if (progress == null || progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
    }
}
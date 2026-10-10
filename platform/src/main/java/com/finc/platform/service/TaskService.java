package com.finc.platform.service;

import com.finc.platform.entity.*;
import com.finc.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final TaskCommentRepository taskCommentRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository,
            CalendarEventRepository calendarEventRepository,
            TaskCommentRepository taskCommentRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.calendarEventRepository = calendarEventRepository;
        this.taskCommentRepository = taskCommentRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

<<<<<<< HEAD
    // Role-based visibility: Admin sees all tasks; Member X sees only their tasks + All-Member tasks
    public List<Task> getTasksForUserView(Long userId) {
        if (userId == null || userId == 1L) {
            return taskRepository.findAll();
        }
        return taskRepository.findVisibleToUser(userId);
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
        if (creator == null) {
            creator = userRepository.findAll().stream().findFirst().orElse(null);
=======
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException("Task not found with ID: " + taskId));
    }

    public Task createTask(Task task, Long assignedUserId) {

        if (assignedUserId != null) {

            User user = userRepository.findById(assignedUserId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with ID: " + assignedUserId
                            ));

            task.setAssignedUser(user);
        }

        if (task.getProgress() == null) {
            task.setProgress(0);
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
        }
        task.setCreatedBy(creator);

<<<<<<< HEAD
        // If assignedUserId is null or <= 0, task is assigned to "All Members"
        if (assignedUserId != null && assignedUserId > 0) {
            task.setAssignedUser(userRepository.findById(assignedUserId).orElse(null));
        } else {
            task.setAssignedUser(null);
        }

        if (eventId != null) {
            task.setRelatedEvent(calendarEventRepository.findById(eventId).orElse(null));
        }

        validateTask(task);
=======
        return taskRepository.save(task);
    }

    public Task updateTask(
            Long taskId,
            Task updatedTask,
            Long assignedUserId
    ) {

        Task existingTask = getTaskById(taskId);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setProgress(updatedTask.getProgress());
        existingTask.setStartDate(updatedTask.getStartDate());
        existingTask.setDueDate(updatedTask.getDueDate());

        if (assignedUserId != null) {

            User user = userRepository.findById(assignedUserId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with ID: " + assignedUserId
                            ));

            existingTask.setAssignedUser(user);
        } else {
            existingTask.setAssignedUser(null);
        }

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long taskId) {

        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException(
                    "Task not found with ID: " + taskId
            );
        }

        taskRepository.deleteById(taskId);
    }

    public Task updateProgress(Long taskId, Integer progress) {
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9

        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
            task.setProgress(100);
        }

        Task savedTask = taskRepository.save(task);

        // Auto-sync deadline to calendar
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
            if (existing.getCompletedAt() == null) existing.setCompletedAt(LocalDateTime.now());
            existing.setProgress(100);
        } else {
            existing.setCompletedAt(null);
        }

        Task saved = taskRepository.save(existing);
        syncTaskDeadlineEvent(saved);
        return saved;
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
<<<<<<< HEAD
        validateProgress(progress);
=======

        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException(
                    "Progress must be between 0 and 100"
            );
        }

>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
        task.setProgress(progress);

        if (progress == 100) {
            task.setStatus(TaskStatus.COMPLETED);
<<<<<<< HEAD
            task.setCompletedAt(LocalDateTime.now());
        } else if (progress > 0) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            task.setCompletedAt(null);
        } else {
            task.setStatus(TaskStatus.TODO);
            task.setCompletedAt(null);
=======
        } else if (progress > 0) {
            task.setStatus(TaskStatus.IN_PROGRESS);
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
        }
        return taskRepository.save(task);
    }

<<<<<<< HEAD
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
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
=======
    public List<Task> getTasksByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        ));

        return taskRepository.findByAssignedUser(user);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
>>>>>>> 5a34969030b895ddc55d00d0646f6cec20c96dc9
    }
}
package com.finc.platform.service;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.entity.User;
import com.finc.platform.repository.TaskRepository;
import com.finc.platform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // GET ALL TASKS
    public List<Task> getAllTasks() {

        return taskRepository.findAll();
    }

    // GET TASKS ASSIGNED TO ONE USER
    public List<Task> getTasksByUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        return taskRepository.findByAssignedUser(user);
    }

    // GET TASK BY ID
    public Task getTaskById(Long taskId) {

        return taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found with ID: " + taskId
                        )
                );
    }

    // CREATE TASK
    public Task createTask(
            Task task,
            Long assignedUserId
    ) {

        if (assignedUserId != null) {

            User user = userRepository.findById(assignedUserId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with ID: "
                                            + assignedUserId
                            )
                    );

            task.setAssignedUser(user);
        }


        if (task.getProgress() == null) {

            task.setProgress(0);
        }

        return taskRepository.save(task);
    }

    // UPDATE TASK
    public Task updateTask(
            Long taskId,
            Task updatedTask,
            Long assignedUserId
    ) {

        Task existingTask = getTaskById(taskId);

        // Update basic task information
        existingTask.setTitle(
                updatedTask.getTitle()
        );

        existingTask.setDescription(
                updatedTask.getDescription()
        );

        existingTask.setStatus(
                updatedTask.getStatus()
        );

        existingTask.setPriority(
                updatedTask.getPriority()
        );

        existingTask.setProgress(
                updatedTask.getProgress()
        );

        existingTask.setStartDate(
                updatedTask.getStartDate()
        );

        existingTask.setDueDate(
                updatedTask.getDueDate()
        );


        // Update assigned user.
        if (assignedUserId != null) {

            User user = userRepository.findById(assignedUserId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "User not found with ID: "
                                            + assignedUserId
                            )
                    );

            existingTask.setAssignedUser(user);

        } else {

            existingTask.setAssignedUser(null);
        }

        return taskRepository.save(existingTask);
    }

    // DELETE TASK
    public void deleteTask(Long taskId) {

        if (!taskRepository.existsById(taskId)) {

            throw new RuntimeException(
                    "Task not found with ID: " + taskId
            );
        }

        taskRepository.deleteById(taskId);
    }
    // UPDATE TASK PROGRESS
    public Task updateProgress(
            Long taskId,
            Integer progress
    ) {

        Task task = getTaskById(taskId);

        // Check progress value
        if (progress == null || progress < 0 || progress > 100) {

            throw new IllegalArgumentException(
                    "Progress must be between 0 and 100"
            );
        }

        // Update progress
        task.setProgress(progress);

        if (progress == 100) {

            task.setStatus(
                    TaskStatus.COMPLETED
            );

        } else if (progress > 0) {

            task.setStatus(
                    TaskStatus.IN_PROGRESS
            );

        } else {

            task.setStatus(
                    TaskStatus.TODO
            );
        }

        return taskRepository.save(task);
    }

    // GET TASKS BY STATUS
    public List<Task> getTasksByStatus(
            TaskStatus status
    ) {

        return taskRepository.findByStatus(status);
    }
}
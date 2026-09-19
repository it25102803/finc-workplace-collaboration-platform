package com.finc.platform.repository;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignedUser(User assignedUser);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByAssignedUserAndStatus(
            User assignedUser,
            TaskStatus status
    );
}
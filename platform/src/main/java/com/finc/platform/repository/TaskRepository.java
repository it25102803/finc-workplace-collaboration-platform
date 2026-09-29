package com.finc.platform.repository;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskStatus;
import com.finc.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // GET TASKS ASSIGNED TO A SPECIFIC USER
    List<Task> findByAssignedUser(User user);

    // GET TASKS BY STATUS
    List<Task> findByStatus(TaskStatus status);
}
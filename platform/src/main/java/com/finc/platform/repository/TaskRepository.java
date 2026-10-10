package com.finc.platform.repository;

import com.finc.platform.entity.Task;
import com.finc.platform.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN t.assignee u WHERE t.assignee IS NULL OR u.id = :userId")
    List<Task> findVisibleToUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignee IS NOT NULL AND t.assignee.id = :userId")
    List<Task> findByAssignedUser_id(@Param("userId") Long userId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByDueDateBeforeAndStatusNotIn(LocalDate date, Collection<TaskStatus> statuses);
}
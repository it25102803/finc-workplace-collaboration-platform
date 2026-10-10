package com.finc.platform.repository;

import com.finc.platform.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTask_TaskIdOrderByCreatedAtAsc(Long taskId);
    List<TaskComment> findByTaskTaskIdOrderByCreatedAtAsc(Long taskId);
}
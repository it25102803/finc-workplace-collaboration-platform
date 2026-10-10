package com.finc.platform.repository;

import com.finc.platform.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTask_TaskId(Long taskId);
    List<TaskAttachment> findByTaskTaskId(Long taskId);
}
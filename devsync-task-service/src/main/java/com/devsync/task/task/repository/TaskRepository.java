package com.devsync.task.task.repository;

import com.devsync.common.enums.TaskStatus;
import com.devsync.task.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);
    Page<Task> findBySprintIdAndDeletedFalse(UUID sprintId, Pageable pageable);
    Optional<Task> findByIdAndDeletedFalse(UUID id);
    List<Task> findByParentIdAndDeletedFalse(UUID parentId);
    long countBySprintIdAndDeletedFalse(UUID sprintId);
    long countBySprintIdAndStatusAndDeletedFalse(UUID sprintId, TaskStatus status);
    List<Task> findBySprintIdAndStatusNotAndDeletedFalse(UUID sprintId, TaskStatus status);
    long countByProjectIdAndDeletedFalse(UUID projectId);
    Page<Task> findByAssigneeIdAndDeletedFalse(UUID assigneeId, Pageable pageable);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.deleted = false GROUP BY t.status")
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Task t WHERE t.projectId = :projectId AND t.deleted = false AND t.assigneeId IS NOT NULL GROUP BY t.assigneeId")
    List<Object[]> countByAssigneeForProject(@Param("projectId") UUID projectId);
}

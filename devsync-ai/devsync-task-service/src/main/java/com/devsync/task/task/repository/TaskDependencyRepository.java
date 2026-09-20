package com.devsync.task.task.repository;

import com.devsync.task.task.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, UUID> {
    List<TaskDependency> findByTaskId(UUID taskId);
    List<TaskDependency> findByDependsOnTaskId(UUID dependsOnTaskId);
    boolean existsByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);

    @Modifying
    @Transactional
    void deleteByTaskIdAndDependsOnTaskId(UUID taskId, UUID dependsOnTaskId);
}

package com.devsync.monolith.task.entity;

import com.devsync.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "task_dependencies", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"task_id", "depends_on_task_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskDependency extends BaseEntity {

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "depends_on_task_id", nullable = false)
    private UUID dependsOnTaskId;

    @Column(nullable = false)
    @Builder.Default
    private String dependencyType = "BLOCKS";
}

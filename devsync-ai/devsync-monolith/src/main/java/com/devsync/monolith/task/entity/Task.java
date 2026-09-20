package com.devsync.monolith.task.entity;

import com.devsync.common.entity.BaseEntity;
import com.devsync.common.enums.Priority;
import com.devsync.common.enums.TaskStatus;
import com.devsync.common.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@SQLRestriction("deleted = false")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task extends BaseEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "sprint_id")
    private UUID sprintId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.BACKLOG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    private Integer storyPoints;
    private LocalDate dueDate;
    private String labels;
}

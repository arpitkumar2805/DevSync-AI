package com.devsync.monolith.task.dto;

import com.devsync.common.enums.Priority;
import com.devsync.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateTaskRequest {
    @NotNull(message = "Project ID is required")
    private UUID projectId;
    private UUID sprintId;
    private UUID parentId;
    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;
    private String description;
    @NotNull(message = "Task type is required")
    private TaskType type;
    private Priority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private String labels;
}

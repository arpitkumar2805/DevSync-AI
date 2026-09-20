package com.devsync.task.task.dto;

import com.devsync.common.enums.Priority;
import lombok.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UpdateTaskRequest {
    private String title;
    private String description;
    private Priority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private String labels;
}

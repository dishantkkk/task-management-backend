package com.dishant.tasks.management.dto;

import com.dishant.tasks.management.model.TaskPriority;
import com.dishant.tasks.management.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;
    private TaskPriority priority;
    private Long assignedToId;
}
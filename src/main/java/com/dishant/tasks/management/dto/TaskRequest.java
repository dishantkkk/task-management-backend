package com.dishant.tasks.management.dto;

import com.dishant.tasks.management.model.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
}
package com.dishant.tasks.management.dto;

import com.dishant.tasks.management.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateTaskRequest {
    @NotNull(message = "Id is required!")
    private Long id;
    @NotBlank(message = "Title can't be blank!")
    private String title;
    @NotBlank(message = "Description can't be blank!")
    private String description;
    private LocalDateTime dueDate;
    @NotBlank(message = "Type can't be blank!")
    private String type;
    @NotBlank(message = "Value can't be blank!")
    private String value;
    private TaskPriority priority;
}

package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.dto.UpdateTaskRequest;
import com.dishant.tasks.management.exception.BadRequestException;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.utils.TaskHelperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.dishant.tasks.management.constants.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskHelperUtil taskHelper;

    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating task...");
        User user = taskHelper.getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .flag(UNFLAGGED)
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("✅ Task created with ID: {}", savedTask.getId());
        return taskHelper.mapToResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks() {
        log.info("Fetching all tasks...");
        User user = taskHelper.getCurrentUser();
        boolean isAdmin = taskHelper.isAdmin(user);

        List<Task> tasks = isAdmin
                ? taskRepository.findByStatusNot(TaskStatus.COMPLETED)
                : taskRepository.findByUserAndStatusNot(user, TaskStatus.COMPLETED);

        log.info("✅ Found {} tasks", tasks.size());
        return tasks.stream().map(taskHelper::mapToResponse).toList();
    }

    public TaskResponse getTaskById(Long id) {
        log.info("Fetching task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        taskHelper.checkAccess(task);
        return taskHelper.mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest updateTaskRequest) {
        log.info("Updating task with ID: {}", id);

        if (updateTaskRequest.getType() == null || updateTaskRequest.getType().isEmpty()) {
            log.warn("❌ Update type is missing for task ID: {}", id);
            throw new BadRequestException("Invalid Request!");
        }

        Task task = taskHelper.getTaskOrThrow(id);
        taskHelper.checkAccess(task);

        switch (updateTaskRequest.getType()) {
            case ACTION -> {
                task.setStatus(TaskStatus.valueOf(updateTaskRequest.getValue()));
                log.info("✅ Task status updated to {}", updateTaskRequest.getValue());
            }
            case FLAG -> {
                task.setFlag(updateTaskRequest.getValue());
                log.info("✅ Task flag updated to {}", updateTaskRequest.getValue());
            }
            default -> {
                task.setTitle(updateTaskRequest.getTitle());
                task.setDescription(updateTaskRequest.getDescription());
                task.setDueDate(updateTaskRequest.getDueDate());
                log.info("✅ Task content updated");
            }
        }

        Task updatedTask = taskRepository.save(task);
        return taskHelper.mapToResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        taskHelper.checkAccess(task);
        taskRepository.delete(task);
        log.info("✅ Task deleted");
    }

    public void closeTask(Long id) {
        log.info("Closing Task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        task.setStatus(TaskStatus.valueOf(COMPLETED));
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);
        log.info("Task with ID: {} marked as COMPLETED.", id);
    }
}

package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.dto.UpdateTaskRequest;
import com.dishant.tasks.management.exception.BadRequestException;
import com.dishant.tasks.management.exception.ResourceNotFoundException;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskPriority;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
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
    private final UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating new task for user");
        User currentUser = taskHelper.getCurrentUser();
        User assignedToUser = currentUser;

        if (taskHelper.isAdmin(currentUser) && request.getAssignedToId() != null) {
            log.debug("Admin user assigning task to user ID: {}", request.getAssignedToId());
            assignedToUser = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> {
                        log.warn("Assigned user not found for ID: {}", request.getAssignedToId());
                        return new ResourceNotFoundException("Assigned user not found");
                    });
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .flag(UNFLAGGED)
                .dueDate(request.getDueDate())
                .user(currentUser)
                .assignedTo(assignedToUser)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .build();

        Task savedTask = taskRepository.save(task);
        log.info("Task created successfully with ID: {}", savedTask.getId());
        return taskHelper.mapToResponse(savedTask);
    }

    public List<TaskResponse> getAllTasks() {
        log.info("Fetching all tasks for current user");
        User user = taskHelper.getCurrentUser();
        boolean isAdmin = taskHelper.isAdmin(user);

        List<Task> tasks = isAdmin
                ? taskRepository.findByStatusNot(TaskStatus.COMPLETED)
                : taskRepository.findByUserAndStatusNot(user, TaskStatus.COMPLETED);

        log.info("Retrieved {} tasks for {}", tasks.size(), isAdmin ? "Admin" : "User");
        return tasks.stream().map(taskHelper::mapToResponse).toList();
    }

    public TaskResponse getTaskById(Long id) {
        log.info("Fetching task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        taskHelper.checkAccess(task);
        log.debug("Access check passed for task ID: {}", id);
        return taskHelper.mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest updateTaskRequest) {
        log.info("Updating task with ID: {}", id);

        if (updateTaskRequest.getType() == null || updateTaskRequest.getType().isEmpty()) {
            log.warn("Update type is missing for task ID: {}", id);
            throw new BadRequestException("Invalid Request!");
        }

        Task task = taskHelper.getTaskOrThrow(id);
        User currentUser = taskHelper.getCurrentUser();
        taskHelper.checkAccess(task);
        log.debug("User {} has access to update task {}", currentUser.getUsername(), id);

        switch (updateTaskRequest.getType()) {
            case ACTION -> {
                log.debug("Updating task status to: {}", updateTaskRequest.getValue());
                task.setStatus(TaskStatus.valueOf(updateTaskRequest.getValue()));
            }
            case FLAG -> {
                log.debug("Updating task flag to: {}", updateTaskRequest.getValue());
                task.setFlag(updateTaskRequest.getValue());
            }
            default -> {
                log.warn("Unknown update type: {}", updateTaskRequest.getType());
                throw new BadRequestException("Unknown update type");
            }
        }

        task.setTitle(updateTaskRequest.getTitle());
        task.setDescription(updateTaskRequest.getDescription());
        task.setDueDate(updateTaskRequest.getDueDate());
        task.setPriority(updateTaskRequest.getPriority());

        if (taskHelper.isAdmin(currentUser) && updateTaskRequest.getAssignedToId() != null) {
            log.debug("Admin reassigning task {} to user ID: {}", id, updateTaskRequest.getAssignedToId());
            User assignedUser = userRepository.findById(updateTaskRequest.getAssignedToId())
                    .orElseThrow(() -> {
                        log.warn("Assigned user not found for update: {}", updateTaskRequest.getAssignedToId());
                        return new ResourceNotFoundException("Assigned user not found");
                    });
            task.setAssignedTo(assignedUser);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task with ID: {} updated successfully", updatedTask.getId());
        return taskHelper.mapToResponse(updatedTask);
    }

    public void deleteTask(Long id) {
        log.info("Deleting task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        taskHelper.checkAccess(task);
        taskRepository.delete(task);
        log.info("Task with ID: {} deleted successfully", id);
    }

    public void closeTask(Long id) {
        log.info("Closing task with ID: {}", id);
        Task task = taskHelper.getTaskOrThrow(id);
        task.setStatus(TaskStatus.COMPLETED);
        task.setUpdatedAt(LocalDateTime.now());

        taskRepository.save(task);
        log.info("Task with ID: {} marked as COMPLETED", id);
    }
}

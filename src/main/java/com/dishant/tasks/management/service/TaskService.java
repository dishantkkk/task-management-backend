package com.dishant.tasks.management.service;


import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.exception.TaskNotFoundException;
import com.dishant.tasks.management.exception.UnAuthorizedException;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse createTask(TaskRequest request) {
        log.info("Creating task...");
        User user = getCurrentUser();

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    public List<TaskResponse> getAllTasks() {
        log.info("Retrieving all tasks...");
        User user = getCurrentUser();
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        List<Task> tasks = isAdmin
                ? taskRepository.findAll()
                : taskRepository.findByUser(user);

        return tasks.stream().map(this::mapToResponse).toList();
    }

    public TaskResponse getTaskById(Long id) {
        log.info("Retrieving task by id...");
        Task task = getTaskOrThrow(id);
        checkAccess(task);
        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        log.info("Updating task...");
        Task task = getTaskOrThrow(id);
        checkAccess(task);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());

        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        log.info("Deleting task...");
        Task task = getTaskOrThrow(id);
        checkAccess(task);
        taskRepository.delete(task);
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id).orElseThrow(() ->
                new TaskNotFoundException("Task not found with id: " + id));
    }

    private void checkAccess(Task task) {
        User user = getCurrentUser();
        boolean isAdmin = user.getRole().name().equals("ADMIN");

        if (!isAdmin && !task.getUser().getId().equals(user.getId())) {
            throw new UnAuthorizedException("Unauthorized access to task");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .username(task.getUser().getUsername())
                .build();
    }
}

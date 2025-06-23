package com.dishant.tasks.management.utils;

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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskHelperUtil {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("❌ Authenticated user not found: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    public boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }

    public Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Task not found with ID: {}", id);
                    return new TaskNotFoundException("Task not found with id: " + id);
                });
    }

    public void checkAccess(Task task) {
        User user = getCurrentUser();
        if (!isAdmin(user) && !task.getUser().getId().equals(user.getId())) {
            log.error("❌ Unauthorized access attempt by user: {}", user.getUsername());
            throw new UnAuthorizedException("Unauthorized access to task");
        }
    }

    public TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .username(task.getUser().getUsername())
                .priority(task.getPriority())
                .build();
    }
}

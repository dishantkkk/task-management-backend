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
import org.springframework.security.core.Authentication;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("❌ No authenticated user found");
            throw new RuntimeException("Unauthorized");
        }

        String identifier = authentication.getName();
        log.debug("🔍 Looking for user by identifier: {}", identifier);

        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> {
                    log.error("❌ Authenticated user not found: {}", identifier);
                    return new UsernameNotFoundException("User not found: " + identifier);
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
                .flag(task.getFlag())
                .username(task.getUser().getUsername())
                .assignedToUsername(task.getAssignedTo() != null ? task.getAssignedTo().getUsername() : null)
                .priority(task.getPriority())
                .build();
    }
}

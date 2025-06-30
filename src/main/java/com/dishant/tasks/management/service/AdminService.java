package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.AdminUserResponse;
import com.dishant.tasks.management.exception.ResourceNotFoundException;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.dishant.tasks.management.constants.Constants.USER_NOT_FOUND_ERROR_MESSAGE;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public AdminUserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(AdminUserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
    }

    public void updateUserRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
        user.setRole(newRole);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
        userRepository.delete(user);
    }

    public Map<String, Object> getDashboardStats() {
        return Map.of(
                "totalUsers", userRepository.count(),
                "totalTasks", taskRepository.count(),
                "completedTasks", taskRepository.countByStatus(TaskStatus.COMPLETED),
                "pendingTasks", taskRepository.countByStatus(TaskStatus.PENDING),
                "inProgressTasks", taskRepository.countByStatus(TaskStatus.IN_PROGRESS)
        );
    }
}


package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.dto.UpdateTaskRequest;
import com.dishant.tasks.management.exception.TaskNotFoundException;
import com.dishant.tasks.management.model.Role;
import com.dishant.tasks.management.model.Task;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.model.User;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private TaskRequest taskRequest;
    private UpdateTaskRequest updateTaskRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .username("john")
                .password("pass")
                .role(Role.USER)
                .build();

        updateTaskRequest = UpdateTaskRequest.builder()
                .title("Test Task")
                .type("flag")
                .description("Description")
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .description("Description")
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        task = Task.builder()
                .id(1L)
                .title(updateTaskRequest.getTitle())
                .description(updateTaskRequest.getDescription())
                .dueDate(updateTaskRequest.getDueDate())
                .user(user)
                .build();

        // Mock SecurityContextHolder
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("john");
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
    }

    @Test
    void testCreateTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(taskRequest);

        assertEquals(task.getTitle(), response.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testGetAllTasks_UserRole() {
        when(taskRepository.findByUser(user)).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void testGetAllTasks_AdminRole() {
        user.setRole(Role.ADMIN);
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskResponse> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void testGetTaskById_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertEquals(task.getTitle(), response.getTitle());
    }

    @Test
    void testGetTaskById_Unauthorized() {
        task.setUser(User.builder().id(2L).username("other").role(Role.USER).build());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> taskService.getTaskById(1L));
        assertEquals("Unauthorized access to task", ex.getMessage());
    }

    @Test
    void testGetTaskById_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void testUpdateTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTask(1L, updateTaskRequest);

        assertEquals(task.getTitle(), response.getTitle());
    }

    @Test
    void testUpdateTask_Unauthorized() {
        task.setUser(User.builder().id(2L).username("other").role(Role.USER).build());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> taskService.updateTask(1L, updateTaskRequest));
    }

    @Test
    void testDeleteTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> taskService.deleteTask(1L));
        verify(taskRepository).delete(task);
    }

    @Test
    void testDeleteTask_Unauthorized() {
        task.setUser(User.builder().id(2L).username("other").role(Role.USER).build());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(1L));
        verify(taskRepository, never()).delete(any());
    }
}

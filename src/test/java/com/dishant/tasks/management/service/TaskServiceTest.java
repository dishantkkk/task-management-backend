package com.dishant.tasks.management.service;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.dto.UpdateTaskRequest;
import com.dishant.tasks.management.exception.BadRequestException;
import com.dishant.tasks.management.exception.ResourceNotFoundException;
import com.dishant.tasks.management.model.*;
import com.dishant.tasks.management.repository.TaskRepository;
import com.dishant.tasks.management.repository.UserRepository;
import com.dishant.tasks.management.utils.TaskHelperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskHelperUtil taskHelper;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private TaskRequest taskRequest;
    private UpdateTaskRequest updateTaskRequest;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .username("john")
                .role(Role.USER)
                .build();

        task = Task.builder()
                .id(1L)
                .title("Old Task")
                .description("Old Desc")
                .user(user)
                .dueDate(LocalDateTime.now().plusDays(2))
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.PENDING)
                .build();

        taskRequest = TaskRequest.builder()
                .title("New Task")
                .description("New Desc")
                .dueDate(LocalDateTime.now().plusDays(3))
                .priority(TaskPriority.HIGH)
                .build();

        updateTaskRequest = UpdateTaskRequest.builder()
                .title("Updated Title")
                .description("Updated Desc")
                .type("action")
                .value("COMPLETED")
                .dueDate(LocalDateTime.now().plusDays(1))
                .priority(TaskPriority.LOW)
                .build();

        taskResponse = TaskResponse.builder()
                .id(1L)
                .title("Mapped Task")
                .description("Mapped Desc")
                .username("john")
                .dueDate(task.getDueDate())
                .status(TaskStatus.PENDING)
                .priority(TaskPriority.MEDIUM)
                .build();
    }

    @Test
    void testCreateTask() {
        when(taskHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskHelper.mapToResponse(any(Task.class))).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(taskRequest);

        assertEquals("Mapped Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testCreateTask_AdminAssignsToAnotherUser() {
        user.setRole(Role.ADMIN);
        User assignee = User.builder().id(2L).username("assignedUser").build();

        taskRequest.setAssignedToId(2L); // triggering the if block

        when(taskHelper.getCurrentUser()).thenReturn(user);
        when(taskHelper.isAdmin(user)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskHelper.mapToResponse(any(Task.class))).thenReturn(taskResponse);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        verify(userRepository).findById(2L); // ensure admin assignment happened
        verify(taskRepository).save(any(Task.class));
    }


    @Test
    void testGetAllTasks_User() {
        when(taskHelper.getCurrentUser()).thenReturn(user);
        when(taskHelper.isAdmin(user)).thenReturn(false);
        when(taskRepository.findByUserAndStatusNot(user, TaskStatus.COMPLETED)).thenReturn(List.of(task));
        when(taskHelper.mapToResponse(any(Task.class))).thenReturn(taskResponse);

        List<TaskResponse> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Mapped Task", result.getFirst().getTitle());
    }

    @Test
    void testGetAllTasks_Admin() {
        user.setRole(Role.ADMIN);
        when(taskHelper.getCurrentUser()).thenReturn(user);
        when(taskHelper.isAdmin(user)).thenReturn(true);
        when(taskRepository.findByStatusNot(TaskStatus.COMPLETED)).thenReturn(List.of(task));
        when(taskHelper.mapToResponse(any(Task.class))).thenReturn(taskResponse);

        List<TaskResponse> result = taskService.getAllTasks();

        assertEquals(1, result.size());
        assertEquals("Mapped Task", result.getFirst().getTitle());
    }

    @Test
    void testGetTaskById_Success() {
        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        doNothing().when(taskHelper).checkAccess(task);
        when(taskHelper.mapToResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.getTaskById(1L);

        assertEquals("Mapped Task", result.getTitle());
    }

    @Test
    void testUpdateTask_WithStatus() {
        updateTaskRequest.setType("action");
        updateTaskRequest.setValue("COMPLETED");

        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        when(taskHelper.getCurrentUser()).thenReturn(user);
        doNothing().when(taskHelper).checkAccess(task);
        when(taskHelper.isAdmin(user)).thenReturn(false);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskHelper.mapToResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.updateTask(1L, updateTaskRequest);

        assertEquals("Mapped Task", result.getTitle());
    }

    @Test
    void testUpdateTask_ReassignByAdmin() {
        updateTaskRequest.setType("flag");
        updateTaskRequest.setValue("flagged");
        updateTaskRequest.setAssignedToId(2L);
        user.setRole(Role.ADMIN);

        User assignee = User.builder().id(2L).username("assigned").build();

        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        when(taskHelper.getCurrentUser()).thenReturn(user);
        doNothing().when(taskHelper).checkAccess(task);
        when(taskHelper.isAdmin(user)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskHelper.mapToResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.updateTask(1L, updateTaskRequest);

        assertEquals("Mapped Task", result.getTitle());
        assertEquals("assigned", task.getAssignedTo().getUsername());
    }

    @Test
    void testUpdateTask_ReassignToInvalidUser() {
        updateTaskRequest.setType("flag");
        updateTaskRequest.setAssignedToId(999L);
        user.setRole(Role.ADMIN);

        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        when(taskHelper.getCurrentUser()).thenReturn(user);
        doNothing().when(taskHelper).checkAccess(task);
        when(taskHelper.isAdmin(user)).thenReturn(true);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, updateTaskRequest));
    }

    @Test
    void testUpdateTask_MissingType_ThrowsBadRequest() {
        updateTaskRequest.setType(null); // or set to ""

        assertThrows(BadRequestException.class, () -> taskService.updateTask(1L, updateTaskRequest));
    }

    @Test
    void testUpdateTask_InvalidType_ThrowsBadRequest() {
        updateTaskRequest.setType("invalidType");

        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        when(taskHelper.getCurrentUser()).thenReturn(user);
        doNothing().when(taskHelper).checkAccess(task);

        assertThrows(BadRequestException.class, () -> taskService.updateTask(1L, updateTaskRequest));
    }

    @Test
    void testDeleteTask() {
        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        doNothing().when(taskHelper).checkAccess(task);

        taskService.deleteTask(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void testCloseTask() {
        when(taskHelper.getTaskOrThrow(1L)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);

        taskService.closeTask(1L);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        verify(taskRepository).save(task);
    }
}

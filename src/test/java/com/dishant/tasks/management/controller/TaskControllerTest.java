package com.dishant.tasks.management.controller;

import com.dishant.tasks.management.dto.TaskRequest;
import com.dishant.tasks.management.dto.TaskResponse;
import com.dishant.tasks.management.dto.UpdateTaskRequest;
import com.dishant.tasks.management.model.TaskStatus;
import com.dishant.tasks.management.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private TaskRequest request;
    private TaskResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = TaskRequest.builder()
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        response = TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.PENDING)
                .dueDate(request.getDueDate())
                .username("testuser")
                .build();
    }

    @Test
    void testCreateTask() {
        when(taskService.createTask(request)).thenReturn(response);

        ResponseEntity<TaskResponse> result = taskController.createTask(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(taskService, times(1)).createTask(request);
    }

    @Test
    void testGetAllTasks() {
        List<TaskResponse> mockTasks = Collections.singletonList(response);
        when(taskService.getAllTasks()).thenReturn(mockTasks);

        ResponseEntity<List<TaskResponse>> result = taskController.getAllTasks();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(response.getTitle(), result.getBody().getFirst().getTitle());
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void testGetTaskById() {
        when(taskService.getTaskById(1L)).thenReturn(response);

        ResponseEntity<TaskResponse> result = taskController.getTaskById(1L);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(response.getTitle(), result.getBody().getTitle());
        verify(taskService, times(1)).getTaskById(1L);
    }

    @Test
    void testUpdateTask() {
        UpdateTaskRequest updatedRequest = UpdateTaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        TaskResponse updatedResponse = TaskResponse.builder()
                .id(1L)
                .title("Updated Task")
                .description("Updated Description")
                .status(TaskStatus.COMPLETED)
                .dueDate(updatedRequest.getDueDate())
                .username("testuser")
                .build();

        when(taskService.updateTask(1L, updatedRequest)).thenReturn(updatedResponse);

        ResponseEntity<TaskResponse> result = taskController.updateTask(1L, updatedRequest);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("Updated Task", result.getBody().getTitle());
        verify(taskService, times(1)).updateTask(1L, updatedRequest);
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskService).deleteTask(1L);

        ResponseEntity<Void> result = taskController.deleteTask(1L);

        assertEquals(204, result.getStatusCode().value());
        verify(taskService, times(1)).deleteTask(1L);
    }
}

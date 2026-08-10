package com.ariscend.backend.service;

import com.ariscend.backend.dto.task.CreateTaskRequest;
import com.ariscend.backend.dto.task.TaskResponse;
import com.ariscend.backend.dto.task.UpdateTaskRequest;
import com.ariscend.backend.dto.task.UpdateTaskStatusRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Task;
import com.ariscend.backend.entity.TaskPriority;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TASK_ID = 10L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createCreatesPendingTaskForExistingUser() {
        AppUser user = user(USER_ID);
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Preparar exposición");
        request.setDescription("Revisar las diapositivas");
        request.setDueDate(LocalDate.of(2026, 8, 15));
        request.setPriority(TaskPriority.HIGH);

        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(TASK_ID);
            task.assignCreatedAt();
            return task;
        });

        TaskResponse response = taskService.create(USER_ID, request);

        assertEquals(TASK_ID, response.getId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals("Preparar exposición", response.getTitle());
        assertEquals(TaskPriority.HIGH, response.getPriority());
        assertFalse(response.isCompleted());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateCompletedChangesOwnedTaskStatus() {
        Task task = task(USER_ID);
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setCompleted(true);

        when(taskRepository.findByIdAndUserId(TASK_ID, USER_ID))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateCompleted(USER_ID, TASK_ID, request);

        assertTrue(response.isCompleted());
        verify(taskRepository).findByIdAndUserId(TASK_ID, USER_ID);
        verify(taskRepository).save(task);
    }

    @Test
    void getByIdDoesNotReturnTaskOwnedByAnotherUser() {
        when(taskRepository.findByIdAndUserId(TASK_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getById(OTHER_USER_ID, TASK_ID)
        );

        assertEquals("Pendiente no encontrado.", exception.getMessage());
        verify(taskRepository).findByIdAndUserId(TASK_ID, OTHER_USER_ID);
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void updateDoesNotModifyTaskOwnedByAnotherUser() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Título ajeno");
        request.setPriority(TaskPriority.LOW);
        when(taskRepository.findByIdAndUserId(TASK_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.update(OTHER_USER_ID, TASK_ID, request)
        );

        verify(taskRepository).findByIdAndUserId(TASK_ID, OTHER_USER_ID);
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void deleteDoesNotDeleteTaskOwnedByAnotherUser() {
        when(taskRepository.findByIdAndUserId(TASK_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.delete(OTHER_USER_ID, TASK_ID)
        );

        verify(taskRepository).findByIdAndUserId(TASK_ID, OTHER_USER_ID);
        verify(taskRepository, never()).save(any(Task.class));
        verify(taskRepository, never()).delete(any(Task.class));
    }

    private AppUser user(Long userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return user;
    }

    private Task task(Long userId) {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setUser(user(userId));
        task.setTitle("Preparar exposición");
        task.setPriority(TaskPriority.MEDIUM);
        task.assignCreatedAt();
        return task;
    }
}

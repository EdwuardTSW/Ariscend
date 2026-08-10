package com.ariscend.backend.service;

import com.ariscend.backend.dto.task.CreateTaskRequest;
import com.ariscend.backend.dto.task.TaskResponse;
import com.ariscend.backend.dto.task.UpdateTaskRequest;
import com.ariscend.backend.dto.task.UpdateTaskStatusRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Task;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public TaskResponse create(Long userId, CreateTaskRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());

        return TaskResponse.from(taskRepository.save(task));
    }

    public List<TaskResponse> getAllByUser(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }

        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse getById(Long userId, Long taskId) {
        return TaskResponse.from(findOwnedTask(userId, taskId));
    }

    @Transactional
    public TaskResponse update(Long userId, Long taskId, UpdateTaskRequest request) {
        Task task = findOwnedTask(userId, taskId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateCompleted(
            Long userId,
            Long taskId,
            UpdateTaskStatusRequest request
    ) {
        Task task = findOwnedTask(userId, taskId);
        task.setCompleted(request.getCompleted());

        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(Long userId, Long taskId) {
        Task task = findOwnedTask(userId, taskId);
        taskRepository.delete(task);
    }

    private Task findOwnedTask(Long userId, Long taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Pendiente no encontrado."));
    }
}

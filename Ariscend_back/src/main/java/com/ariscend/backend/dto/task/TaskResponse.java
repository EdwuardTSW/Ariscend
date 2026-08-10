package com.ariscend.backend.dto.task;

import com.ariscend.backend.entity.Task;
import com.ariscend.backend.entity.TaskPriority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;
    private TaskPriority priority;
    private LocalDateTime createdAt;

    public static TaskResponse from(Task task) {
        TaskResponse response = new TaskResponse();

        response.id = task.getId();
        response.userId = task.getUser().getId();
        response.title = task.getTitle();
        response.description = task.getDescription();
        response.completed = task.isCompleted();
        response.dueDate = task.getDueDate();
        response.priority = task.getPriority();
        response.createdAt = task.getCreatedAt();

        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

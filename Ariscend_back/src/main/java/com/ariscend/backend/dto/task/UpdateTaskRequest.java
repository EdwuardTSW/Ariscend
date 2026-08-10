package com.ariscend.backend.dto.task;

import com.ariscend.backend.entity.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateTaskRequest {

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 150, message = "El título no puede superar los 150 caracteres.")
    private String title;

    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres.")
    private String description;

    private LocalDate dueDate;

    @NotNull(message = "La prioridad es obligatoria.")
    private TaskPriority priority;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
}

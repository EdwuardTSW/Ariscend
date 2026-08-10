package com.ariscend.backend.dto.task;

import jakarta.validation.constraints.NotNull;

public class UpdateTaskStatusRequest {

    @NotNull(message = "El estado es obligatorio.")
    private Boolean completed;

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}

package com.ariscend.backend.dto.completion;

import com.ariscend.backend.entity.HabitCompletion;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HabitCompletionResponse {

    private Long id;
    private LocalDate completedDate;
    private LocalDateTime completedAt;
    private String notes;

    public static HabitCompletionResponse from(HabitCompletion completion) {
        HabitCompletionResponse response = new HabitCompletionResponse();

        response.id = completion.getId();
        response.completedDate = completion.getCompletedDate();
        response.completedAt = completion.getCompletedAt();
        response.notes = completion.getNotes();

        return response;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getNotes() {
        return notes;
    }
}
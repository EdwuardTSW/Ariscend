package com.ariscend.backend.dto.completion;

import java.time.LocalDate;

public class CompleteHabitRequest {

    private LocalDate completedDate;
    private String notes;

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
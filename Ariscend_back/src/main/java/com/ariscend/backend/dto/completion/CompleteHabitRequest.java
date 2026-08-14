package com.ariscend.backend.dto.completion;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CompleteHabitRequest {

    @PastOrPresent(message = "La fecha de finalización no puede ser futura.")
    private LocalDate completedDate;

    @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres.")
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

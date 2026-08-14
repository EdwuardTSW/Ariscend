package com.ariscend.backend.dto.note;

import jakarta.validation.constraints.NotNull;

public class UpdateNoteArchivedRequest {

    @NotNull(message = "El estado archivado es obligatorio.")
    private Boolean archived;

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}

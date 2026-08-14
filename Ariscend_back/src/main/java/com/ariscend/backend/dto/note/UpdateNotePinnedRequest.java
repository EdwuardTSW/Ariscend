package com.ariscend.backend.dto.note;

import jakarta.validation.constraints.NotNull;

public class UpdateNotePinnedRequest {

    @NotNull(message = "El estado fijado es obligatorio.")
    private Boolean pinned;

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }
}

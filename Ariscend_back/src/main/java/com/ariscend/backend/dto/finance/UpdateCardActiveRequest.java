package com.ariscend.backend.dto.finance;

import jakarta.validation.constraints.NotNull;

public class UpdateCardActiveRequest {
    @NotNull(message = "El estado activo es obligatorio.")
    private Boolean active;
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

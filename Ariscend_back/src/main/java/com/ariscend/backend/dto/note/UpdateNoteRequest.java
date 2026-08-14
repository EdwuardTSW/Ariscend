package com.ariscend.backend.dto.note;

import jakarta.validation.constraints.Size;

public class UpdateNoteRequest {

    @Size(max = 150, message = "El título no puede superar los 150 caracteres.")
    private String title;

    @Size(max = 50000, message = "El contenido no puede superar los 50000 caracteres.")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

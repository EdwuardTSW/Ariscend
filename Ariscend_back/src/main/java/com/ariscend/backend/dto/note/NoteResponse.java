package com.ariscend.backend.dto.note;

import com.ariscend.backend.entity.Note;

import java.time.LocalDateTime;

public class NoteResponse {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private boolean pinned;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NoteResponse from(Note note) {
        NoteResponse response = new NoteResponse();
        response.id = note.getId();
        response.userId = note.getUser().getId();
        response.title = note.getTitle();
        response.content = note.getContent();
        response.pinned = note.isPinned();
        response.archived = note.isArchived();
        response.createdAt = note.getCreatedAt();
        response.updatedAt = note.getUpdatedAt();
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

    public String getContent() {
        return content;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

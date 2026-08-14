package com.ariscend.backend.service;

import com.ariscend.backend.dto.common.PagedResponse;
import com.ariscend.backend.dto.note.CreateNoteRequest;
import com.ariscend.backend.dto.note.NoteResponse;
import com.ariscend.backend.dto.note.UpdateNoteArchivedRequest;
import com.ariscend.backend.dto.note.UpdateNotePinnedRequest;
import com.ariscend.backend.dto.note.UpdateNoteRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Note;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long NOTE_ID = 10L;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void createAllowsEmptyDraftForExistingUser() {
        AppUser user = user(USER_ID);
        CreateNoteRequest request = new CreateNoteRequest();
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(NOTE_ID);
            note.assignTimestamps();
            return note;
        });

        NoteResponse response = noteService.create(USER_ID, request);

        assertEquals(NOTE_ID, response.getId());
        assertEquals(USER_ID, response.getUserId());
        assertEquals("", response.getContent());
        assertFalse(response.isPinned());
        assertFalse(response.isArchived());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    void getAllReturnsPagedNotesAndNormalizesSearch() {
        Note note = note(USER_ID);
        when(appUserRepository.existsById(USER_ID)).thenReturn(true);
        when(noteRepository.findByUserAndFilters(
                eq(USER_ID),
                eq(true),
                eq(false),
                eq("reunión"),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(note)));

        PagedResponse<NoteResponse> response = noteService.getAllByUser(
                USER_ID,
                "  reunión  ",
                true,
                false,
                0,
                20
        );

        assertEquals(1, response.getContent().size());
        assertEquals(NOTE_ID, response.getContent().get(0).getId());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void updateChangesOwnedNoteContent() {
        Note note = note(USER_ID);
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle("Reunión actualizada");
        request.setContent("Nuevos acuerdos");
        when(noteRepository.findByIdAndUserId(NOTE_ID, USER_ID)).thenReturn(Optional.of(note));
        when(noteRepository.saveAndFlush(note)).thenAnswer(invocation -> {
            note.updateTimestamp();
            return note;
        });

        NoteResponse response = noteService.update(USER_ID, NOTE_ID, request);

        assertEquals("Reunión actualizada", response.getTitle());
        assertEquals("Nuevos acuerdos", response.getContent());
        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void updatePinnedChangesOwnedNoteStatus() {
        Note note = note(USER_ID);
        UpdateNotePinnedRequest request = new UpdateNotePinnedRequest();
        request.setPinned(true);
        when(noteRepository.findByIdAndUserId(NOTE_ID, USER_ID)).thenReturn(Optional.of(note));
        when(noteRepository.saveAndFlush(note)).thenAnswer(invocation -> {
            note.updateTimestamp();
            return note;
        });

        NoteResponse response = noteService.updatePinned(USER_ID, NOTE_ID, request);

        assertTrue(response.isPinned());
        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void updateArchivedChangesOwnedNoteStatus() {
        Note note = note(USER_ID);
        UpdateNoteArchivedRequest request = new UpdateNoteArchivedRequest();
        request.setArchived(true);
        when(noteRepository.findByIdAndUserId(NOTE_ID, USER_ID)).thenReturn(Optional.of(note));
        when(noteRepository.saveAndFlush(note)).thenAnswer(invocation -> {
            note.updateTimestamp();
            return note;
        });

        NoteResponse response = noteService.updateArchived(USER_ID, NOTE_ID, request);

        assertTrue(response.isArchived());
        verify(noteRepository).saveAndFlush(note);
    }

    @Test
    void getByIdDoesNotReturnNoteOwnedByAnotherUser() {
        when(noteRepository.findByIdAndUserId(NOTE_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.getById(OTHER_USER_ID, NOTE_ID)
        );

        assertEquals("Nota no encontrada.", exception.getMessage());
        verify(noteRepository).findByIdAndUserId(NOTE_ID, OTHER_USER_ID);
        verify(noteRepository, never()).saveAndFlush(any(Note.class));
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void updatePinnedDoesNotModifyNoteOwnedByAnotherUser() {
        UpdateNotePinnedRequest request = new UpdateNotePinnedRequest();
        request.setPinned(true);
        when(noteRepository.findByIdAndUserId(NOTE_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updatePinned(OTHER_USER_ID, NOTE_ID, request)
        );

        verify(noteRepository).findByIdAndUserId(NOTE_ID, OTHER_USER_ID);
        verify(noteRepository, never()).saveAndFlush(any(Note.class));
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void updateArchivedDoesNotModifyNoteOwnedByAnotherUser() {
        UpdateNoteArchivedRequest request = new UpdateNoteArchivedRequest();
        request.setArchived(true);
        when(noteRepository.findByIdAndUserId(NOTE_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updateArchived(OTHER_USER_ID, NOTE_ID, request)
        );

        verify(noteRepository).findByIdAndUserId(NOTE_ID, OTHER_USER_ID);
        verify(noteRepository, never()).saveAndFlush(any(Note.class));
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void updateDoesNotModifyNoteOwnedByAnotherUser() {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setContent("Contenido ajeno");
        when(noteRepository.findByIdAndUserId(NOTE_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.update(OTHER_USER_ID, NOTE_ID, request)
        );

        verify(noteRepository).findByIdAndUserId(NOTE_ID, OTHER_USER_ID);
        verify(noteRepository, never()).saveAndFlush(any(Note.class));
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteDoesNotDeleteNoteOwnedByAnotherUser() {
        when(noteRepository.findByIdAndUserId(NOTE_ID, OTHER_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.delete(OTHER_USER_ID, NOTE_ID)
        );

        verify(noteRepository).findByIdAndUserId(NOTE_ID, OTHER_USER_ID);
        verify(noteRepository, never()).saveAndFlush(any(Note.class));
        verify(noteRepository, never()).delete(any(Note.class));
    }

    private AppUser user(Long userId) {
        AppUser user = new AppUser();
        user.setId(userId);
        return user;
    }

    private Note note(Long userId) {
        Note note = new Note();
        note.setId(NOTE_ID);
        note.setUser(user(userId));
        note.setTitle("Reunión");
        note.setContent("Acuerdos pendientes");
        note.assignTimestamps();
        return note;
    }
}

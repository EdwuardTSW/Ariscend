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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class NoteService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NoteRepository noteRepository;
    private final AppUserRepository appUserRepository;

    public NoteService(NoteRepository noteRepository, AppUserRepository appUserRepository) {
        this.noteRepository = noteRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public NoteResponse create(Long userId, CreateNoteRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        Note note = new Note();
        note.setUser(user);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent() == null ? "" : request.getContent());

        return NoteResponse.from(noteRepository.save(note));
    }

    public PagedResponse<NoteResponse> getAllByUser(
            Long userId,
            String query,
            Boolean pinned,
            Boolean archived,
            int page,
            int size
    ) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
        validatePagination(page, size);

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("pinned"),
                        Sort.Order.desc("updatedAt"),
                        Sort.Order.desc("id")
                )
        );
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        Specification<Note> filters = (root, criteriaQuery, builder) ->
                builder.equal(root.get("user").get("id"), userId);
        if (pinned != null) filters = filters.and((root, criteriaQuery, builder) ->
                builder.equal(root.get("pinned"), pinned));
        if (archived != null) filters = filters.and((root, criteriaQuery, builder) ->
                builder.equal(root.get("archived"), archived));
        if (normalizedQuery != null) {
            String searchPattern = "%" + normalizedQuery.toLowerCase() + "%";
            filters = filters.and((root, criteriaQuery, builder) -> builder.or(
                    builder.like(builder.lower(root.get("title")), searchPattern),
                    builder.like(builder.lower(root.get("content")), searchPattern)
            ));
        }
        Page<NoteResponse> notes = noteRepository.findAll(filters, pageable)
                .map(NoteResponse::from);

        return PagedResponse.from(notes);
    }

    public NoteResponse getById(Long userId, Long noteId) {
        return NoteResponse.from(findOwnedNote(userId, noteId));
    }

    @Transactional
    public NoteResponse update(Long userId, Long noteId, UpdateNoteRequest request) {
        Note note = findOwnedNote(userId, noteId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent() == null ? "" : request.getContent());

        return NoteResponse.from(noteRepository.saveAndFlush(note));
    }

    @Transactional
    public NoteResponse updatePinned(
            Long userId,
            Long noteId,
            UpdateNotePinnedRequest request
    ) {
        Note note = findOwnedNote(userId, noteId);
        note.setPinned(request.getPinned());

        return NoteResponse.from(noteRepository.saveAndFlush(note));
    }

    @Transactional
    public NoteResponse updateArchived(
            Long userId,
            Long noteId,
            UpdateNoteArchivedRequest request
    ) {
        Note note = findOwnedNote(userId, noteId);
        note.setArchived(request.getArchived());

        return NoteResponse.from(noteRepository.saveAndFlush(note));
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        Note note = findOwnedNote(userId, noteId);
        noteRepository.delete(note);
    }

    private Note findOwnedNote(Long userId, Long noteId) {
        return noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota no encontrada."));
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100.");
        }
    }
}

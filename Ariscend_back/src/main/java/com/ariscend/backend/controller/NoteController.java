package com.ariscend.backend.controller;

import com.ariscend.backend.dto.common.PagedResponse;
import com.ariscend.backend.dto.note.CreateNoteRequest;
import com.ariscend.backend.dto.note.NoteResponse;
import com.ariscend.backend.dto.note.UpdateNoteArchivedRequest;
import com.ariscend.backend.dto.note.UpdateNotePinnedRequest;
import com.ariscend.backend.dto.note.UpdateNoteRequest;
import com.ariscend.backend.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/notes")
@Tag(name = "Notas")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nota o borrador vacío")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Nota creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public NoteResponse create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateNoteRequest request
    ) {
        return noteService.create(userId, request);
    }

    @GetMapping
    @Operation(summary = "Listar y buscar las notas de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notas encontradas"),
            @ApiResponse(responseCode = "400", description = "Paginación inválida"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public PagedResponse<NoteResponse> getAllByUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "false") Boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return noteService.getAllByUser(userId, query, pinned, archived, page, size);
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Consultar una nota por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota encontrada"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    })
    public NoteResponse getById(@PathVariable Long userId, @PathVariable Long noteId) {
        return noteService.getById(userId, noteId);
    }

    @PutMapping("/{noteId}")
    @Operation(summary = "Actualizar el contenido de una nota")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nota actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    })
    public NoteResponse update(
            @PathVariable Long userId,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteRequest request
    ) {
        return noteService.update(userId, noteId, request);
    }

    @PatchMapping("/{noteId}/pinned")
    @Operation(summary = "Fijar o dejar de fijar una nota")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado fijado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    })
    public NoteResponse updatePinned(
            @PathVariable Long userId,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNotePinnedRequest request
    ) {
        return noteService.updatePinned(userId, noteId, request);
    }

    @PatchMapping("/{noteId}/archived")
    @Operation(summary = "Archivar o restaurar una nota")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado archivado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    })
    public NoteResponse updateArchived(
            @PathVariable Long userId,
            @PathVariable Long noteId,
            @Valid @RequestBody UpdateNoteArchivedRequest request
    ) {
        return noteService.updateArchived(userId, noteId, request);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar una nota")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Nota eliminada"),
            @ApiResponse(responseCode = "404", description = "Nota no encontrada")
    })
    public void delete(@PathVariable Long userId, @PathVariable Long noteId) {
        noteService.delete(userId, noteId);
    }
}

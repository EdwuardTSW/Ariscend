package com.ariscend.backend.controller;

import com.ariscend.backend.dto.completion.CompleteHabitRequest;
import com.ariscend.backend.dto.completion.HabitCompletionResponse;
import com.ariscend.backend.dto.habit.CreateHabitRequest;
import com.ariscend.backend.dto.habit.HabitResponse;
import com.ariscend.backend.service.HabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/habits")
@Tag(name = "Hábitos")
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping
    @Operation(summary = "Listar los hábitos activos de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábitos encontrados"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public List<HabitResponse> getAllByUser(@PathVariable Long userId) {
        return habitService.getAllByUser(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hábito creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public HabitResponse create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateHabitRequest request
    ) {
        return habitService.create(userId, request);
    }

    @PostMapping("/{habitId}/complete")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar la finalización de un hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hábito completado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Hábito no encontrado"),
            @ApiResponse(responseCode = "409", description = "El hábito ya fue completado en esa fecha")
    })
    public HabitCompletionResponse complete(
            @PathVariable Long habitId,
            @PathVariable Long userId,
            @Valid @RequestBody CompleteHabitRequest request
    ) {
        return habitService.complete(userId, habitId, request);
    }

    @GetMapping("/{habitId}/completions")
    @Operation(summary = "Consultar el historial de finalizaciones de un hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Finalizaciones encontradas"),
            @ApiResponse(responseCode = "404", description = "Hábito no encontrado")
    })
    public List<HabitCompletionResponse> getCompletions(
            @PathVariable Long userId,
            @PathVariable Long habitId
    ) {
        return habitService.getCompletions(userId, habitId);
    }

    @DeleteMapping("/{habitId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desactivar un hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hábito desactivado"),
            @ApiResponse(responseCode = "404", description = "Hábito no encontrado")
    })
    public void deactivate(@PathVariable Long userId, @PathVariable Long habitId) {
        habitService.deactivate(userId, habitId);
    }
}

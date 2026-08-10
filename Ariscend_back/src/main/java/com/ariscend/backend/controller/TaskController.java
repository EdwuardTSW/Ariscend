package com.ariscend.backend.controller;

import com.ariscend.backend.dto.task.CreateTaskRequest;
import com.ariscend.backend.dto.task.TaskResponse;
import com.ariscend.backend.dto.task.UpdateTaskRequest;
import com.ariscend.backend.dto.task.UpdateTaskStatusRequest;
import com.ariscend.backend.service.TaskService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/tasks")
@Tag(name = "Pendientes")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un pendiente para un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pendiente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public TaskResponse create(
            @PathVariable Long userId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return taskService.create(userId, request);
    }

    @GetMapping
    @Operation(summary = "Listar los pendientes de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pendientes encontrados"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public List<TaskResponse> getAllByUser(@PathVariable Long userId) {
        return taskService.getAllByUser(userId);
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Consultar un pendiente por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pendiente encontrado"),
            @ApiResponse(responseCode = "404", description = "Pendiente no encontrado")
    })
    public TaskResponse getById(@PathVariable Long userId, @PathVariable Long taskId) {
        return taskService.getById(userId, taskId);
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Actualizar un pendiente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pendiente actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pendiente no encontrado")
    })
    public TaskResponse update(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(userId, taskId, request);
    }

    @PatchMapping("/{taskId}/completed")
    @Operation(summary = "Cambiar el estado completado de un pendiente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "404", description = "Pendiente no encontrado")
    })
    public TaskResponse updateCompleted(
            @PathVariable Long userId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        return taskService.updateCompleted(userId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un pendiente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pendiente eliminado"),
            @ApiResponse(responseCode = "404", description = "Pendiente no encontrado")
    })
    public void delete(@PathVariable Long userId, @PathVariable Long taskId) {
        taskService.delete(userId, taskId);
    }
}

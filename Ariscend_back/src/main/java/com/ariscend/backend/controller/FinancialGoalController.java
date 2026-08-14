package com.ariscend.backend.controller;

import com.ariscend.backend.dto.finance.*;
import com.ariscend.backend.entity.GoalStatus;
import com.ariscend.backend.service.FinancialGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/finance/goals")
@Tag(name = "Finanzas - Metas")
public class FinancialGoalController {
    private final FinancialGoalService service;
    public FinancialGoalController(FinancialGoalService service) { this.service = service; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Crear una meta financiera personalizada")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Meta creada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public FinancialGoalResponse create(@PathVariable Long userId, @Valid @RequestBody CreateFinancialGoalRequest request) { return service.create(userId, request); }

    @GetMapping @Operation(summary = "Listar metas financieras por estado")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Metas encontradas"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public List<FinancialGoalResponse> getAll(@PathVariable Long userId, @RequestParam(required = false) GoalStatus status) { return service.getAll(userId, status); }

    @GetMapping("/{goalId}") @Operation(summary = "Consultar una meta y su progreso")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meta encontrada"), @ApiResponse(responseCode = "404", description = "Meta no encontrada")})
    public FinancialGoalResponse getById(@PathVariable Long userId, @PathVariable Long goalId) { return service.getById(userId, goalId); }

    @PutMapping("/{goalId}") @Operation(summary = "Actualizar una meta financiera")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meta actualizada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Meta no encontrada"), @ApiResponse(responseCode = "409", description = "Cambio no permitido")})
    public FinancialGoalResponse update(@PathVariable Long userId, @PathVariable Long goalId, @Valid @RequestBody UpdateFinancialGoalRequest request) { return service.update(userId, goalId, request); }

    @DeleteMapping("/{goalId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Cancelar una meta conservando sus aportes")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Meta cancelada"), @ApiResponse(responseCode = "404", description = "Meta no encontrada")})
    public void cancel(@PathVariable Long userId, @PathVariable Long goalId) { service.cancel(userId, goalId); }

    @PostMapping("/{goalId}/contributions") @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Registrar un aporte y crear su gasto automáticamente")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Aporte y gasto creados"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Meta o tarjeta no encontrada"), @ApiResponse(responseCode = "409", description = "Meta no activa")})
    public GoalContributionResponse addContribution(@PathVariable Long userId, @PathVariable Long goalId,
            @Valid @RequestBody CreateGoalContributionRequest request) { return service.addContribution(userId, goalId, request); }

    @GetMapping("/{goalId}/contributions") @Operation(summary = "Consultar el historial de aportes de una meta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Aportes encontrados"), @ApiResponse(responseCode = "404", description = "Meta no encontrada")})
    public List<GoalContributionResponse> getContributions(@PathVariable Long userId, @PathVariable Long goalId) { return service.getContributions(userId, goalId); }

    @DeleteMapping("/{goalId}/contributions/{contributionId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Cancelar un aporte y su gasto enlazado")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Aporte y gasto cancelados"), @ApiResponse(responseCode = "404", description = "Aporte no encontrado")})
    public void cancelContribution(@PathVariable Long userId, @PathVariable Long goalId, @PathVariable Long contributionId) { service.cancelContribution(userId, goalId, contributionId); }
}

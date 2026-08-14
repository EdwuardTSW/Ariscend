package com.ariscend.backend.controller;

import com.ariscend.backend.dto.finance.*;
import com.ariscend.backend.dto.common.PagedResponse;
import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.service.CardService;
import com.ariscend.backend.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/finance/cards")
@Tag(name = "Finanzas - Tarjetas")
public class CardController {
    private final CardService service;
    private final FinancialTransactionService transactionService;
    public CardController(CardService service, FinancialTransactionService transactionService) {
        this.service = service; this.transactionService = transactionService;
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Registrar una tarjeta de crédito o débito")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Tarjeta creada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public CardResponse create(@PathVariable Long userId, @Valid @RequestBody CreateCardRequest request) { return service.create(userId, request); }

    @GetMapping @Operation(summary = "Listar las tarjetas de un usuario")
    @ApiResponse(responseCode = "200", description = "Tarjetas encontradas")
    public List<CardResponse> getAll(@PathVariable Long userId) { return service.getAll(userId); }

    @GetMapping("/{cardId}") @Operation(summary = "Consultar una tarjeta por id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tarjeta encontrada"), @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")})
    public CardResponse getById(@PathVariable Long userId, @PathVariable Long cardId) { return service.getById(userId, cardId); }

    @PutMapping("/{cardId}") @Operation(summary = "Actualizar una tarjeta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tarjeta actualizada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada"), @ApiResponse(responseCode = "409", description = "Cambio no permitido")})
    public CardResponse update(@PathVariable Long userId, @PathVariable Long cardId, @Valid @RequestBody UpdateCardRequest request) { return service.update(userId, cardId, request); }

    @PatchMapping("/{cardId}/active") @Operation(summary = "Activar o desactivar una tarjeta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estado actualizado"), @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")})
    public CardResponse updateActive(@PathVariable Long userId, @PathVariable Long cardId, @Valid @RequestBody UpdateCardActiveRequest request) { return service.updateActive(userId, cardId, request); }

    @GetMapping("/{cardId}/summary")
    @Operation(
            summary = "Consultar saldo, deuda, fechas y alerta de una tarjeta",
            description = "Las alertas son aproximadas y se calculan con la deuda total; no representan un estado de cuenta bancario."
    )
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Resumen calculado"), @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")})
    public CardSummaryResponse getSummary(@PathVariable Long userId, @PathVariable Long cardId) { return service.getSummary(userId, cardId); }

    @GetMapping("/{cardId}/transactions") @Operation(summary = "Consultar los movimientos de una tarjeta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimientos encontrados"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public PagedResponse<FinancialTransactionResponse> getTransactions(
            @PathVariable Long userId,
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "ACTIVE") FinancialStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        service.getById(userId, cardId);
        return transactionService.getAll(userId, null, null, cardId, null, null, null, status, page, size);
    }

    @DeleteMapping("/{cardId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Cancelar una tarjeta conservando su historial")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Tarjeta cancelada"), @ApiResponse(responseCode = "404", description = "Tarjeta no encontrada")})
    public void cancel(@PathVariable Long userId, @PathVariable Long cardId) { service.cancel(userId, cardId); }
}

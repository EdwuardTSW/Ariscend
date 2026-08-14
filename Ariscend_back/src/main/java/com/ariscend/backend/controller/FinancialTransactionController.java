package com.ariscend.backend.controller;

import com.ariscend.backend.dto.common.PagedResponse;
import com.ariscend.backend.dto.finance.CreateFinancialTransactionRequest;
import com.ariscend.backend.dto.finance.FinanceSummaryResponse;
import com.ariscend.backend.dto.finance.FinancialTransactionResponse;
import com.ariscend.backend.dto.finance.UpdateFinancialTransactionRequest;
import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.TransactionType;
import com.ariscend.backend.service.FinancialTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/finance")
@Tag(name = "Finanzas - Movimientos")
public class FinancialTransactionController {
    private final FinancialTransactionService service;
    public FinancialTransactionController(FinancialTransactionService service) { this.service = service; }

    @PostMapping("/transactions") @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Registrar un ingreso, gasto o pago de tarjeta")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Movimiento creado"), @ApiResponse(responseCode = "400", description = "Datos o relaciones inválidos"), @ApiResponse(responseCode = "404", description = "Usuario, categoría o tarjeta no encontrada"), @ApiResponse(responseCode = "409", description = "Saldo, crédito o deuda insuficiente para la operación")})
    public FinancialTransactionResponse create(@PathVariable Long userId, @Valid @RequestBody CreateFinancialTransactionRequest request) { return service.create(userId, request); }

    @GetMapping("/transactions") @Operation(summary = "Listar y filtrar movimientos financieros")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimientos encontrados"), @ApiResponse(responseCode = "400", description = "Filtros inválidos"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public PagedResponse<FinancialTransactionResponse> getAll(@PathVariable Long userId,
            @RequestParam(required = false) TransactionType type, @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long cardId, @RequestParam(required = false) String currency,
            @RequestParam(required = false) LocalDate dateFrom, @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "ACTIVE") FinancialStatus status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.getAll(userId, type, categoryId, cardId, currency, dateFrom, dateTo, status, page, size);
    }

    @GetMapping("/transactions/{transactionId}") @Operation(summary = "Consultar un movimiento por id")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimiento encontrado"), @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")})
    public FinancialTransactionResponse getById(@PathVariable Long userId, @PathVariable Long transactionId) { return service.getById(userId, transactionId); }

    @PutMapping("/transactions/{transactionId}") @Operation(summary = "Actualizar un movimiento financiero")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimiento actualizado"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"), @ApiResponse(responseCode = "409", description = "Movimiento no editable")})
    public FinancialTransactionResponse update(@PathVariable Long userId, @PathVariable Long transactionId,
            @Valid @RequestBody UpdateFinancialTransactionRequest request) { return service.update(userId, transactionId, request); }

    @DeleteMapping("/transactions/{transactionId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Cancelar un movimiento conservando su historial")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Movimiento cancelado"), @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"), @ApiResponse(responseCode = "409", description = "Debe cancelarse desde la meta")})
    public void cancel(@PathVariable Long userId, @PathVariable Long transactionId) { service.cancel(userId, transactionId); }

    @GetMapping("/summary") @Operation(summary = "Consultar el resumen de balance de un período")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Resumen calculado en moneda base"), @ApiResponse(responseCode = "400", description = "Período inválido"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public FinanceSummaryResponse getSummary(@PathVariable Long userId, @RequestParam LocalDate dateFrom, @RequestParam LocalDate dateTo) {
        return service.getSummary(userId, dateFrom, dateTo);
    }
}

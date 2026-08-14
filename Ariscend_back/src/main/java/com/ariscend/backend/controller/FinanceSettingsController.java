package com.ariscend.backend.controller;

import com.ariscend.backend.dto.finance.FinanceSettingsResponse;
import com.ariscend.backend.dto.finance.UpdateFinanceSettingsRequest;
import com.ariscend.backend.service.FinanceSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/finance/settings")
@Tag(name = "Finanzas - Configuración")
public class FinanceSettingsController {
    private final FinanceSettingsService service;
    public FinanceSettingsController(FinanceSettingsService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "Consultar la configuración financiera")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Configuración encontrada"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public FinanceSettingsResponse get(@PathVariable Long userId) { return service.get(userId); }

    @PutMapping
    @Operation(summary = "Actualizar moneda base y días de alerta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Configuración actualizada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "409", description = "La moneda base ya no puede cambiarse")})
    public FinanceSettingsResponse update(@PathVariable Long userId, @Valid @RequestBody UpdateFinanceSettingsRequest request) {
        return service.update(userId, request);
    }
}

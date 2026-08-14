package com.ariscend.backend.controller;

import com.ariscend.backend.dto.finance.CategoryResponse;
import com.ariscend.backend.dto.finance.CreateCategoryRequest;
import com.ariscend.backend.dto.finance.UpdateCategoryRequest;
import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.service.TransactionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/finance/categories")
@Tag(name = "Finanzas - Categorías")
public class TransactionCategoryController {
    private final TransactionCategoryService service;
    public TransactionCategoryController(TransactionCategoryService service) { this.service = service; }

    @GetMapping @Operation(summary = "Listar categorías fijas y personalizadas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Categorías encontradas"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public List<CategoryResponse> getAll(@PathVariable Long userId, @RequestParam(required = false) CategoryType type) { return service.getAll(userId, type); }

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Crear una categoría personalizada")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Categoría creada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    public CategoryResponse create(@PathVariable Long userId, @Valid @RequestBody CreateCategoryRequest request) { return service.create(userId, request); }

    @PutMapping("/{categoryId}") @Operation(summary = "Actualizar una categoría personalizada")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Categoría actualizada"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "404", description = "Categoría personalizada no encontrada")})
    public CategoryResponse update(@PathVariable Long userId, @PathVariable Long categoryId, @Valid @RequestBody UpdateCategoryRequest request) { return service.update(userId, categoryId, request); }

    @DeleteMapping("/{categoryId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Desactivar una categoría personalizada")
    @ApiResponse(responseCode = "204", description = "Categoría desactivada")
    public void deactivate(@PathVariable Long userId, @PathVariable Long categoryId) { service.deactivate(userId, categoryId); }
}

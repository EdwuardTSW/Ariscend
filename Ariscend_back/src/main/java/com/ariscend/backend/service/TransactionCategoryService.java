package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CategoryResponse;
import com.ariscend.backend.dto.finance.CreateCategoryRequest;
import com.ariscend.backend.dto.finance.UpdateCategoryRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.TransactionCategory;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.TransactionCategoryRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TransactionCategoryService {

    private final TransactionCategoryRepository categoryRepository;
    private final AppUserRepository appUserRepository;
    private final FinancialTransactionRepository transactionRepository;

    public TransactionCategoryService(
            TransactionCategoryRepository categoryRepository,
            AppUserRepository appUserRepository,
            FinancialTransactionRepository transactionRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.appUserRepository = appUserRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<CategoryResponse> getAll(Long userId, CategoryType type) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado.");
        }
        return categoryRepository.findAvailable(userId, type)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long userId, CreateCategoryRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        ensureUniqueName(userId, request.getType(), request.getName(), null);
        TransactionCategory category = new TransactionCategory();
        category.setUser(user);
        category.setName(request.getName().trim());
        category.setType(request.getType());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long userId, Long categoryId, UpdateCategoryRequest request) {
        TransactionCategory category = findOwnedCustomCategory(userId, categoryId);
        ensureUniqueName(userId, request.getType(), request.getName(), categoryId);
        if (category.getType() != request.getType() && transactionRepository.existsByCategoryId(categoryId)) {
            throw new IllegalStateException("No se puede cambiar el tipo de una categoría con movimientos.");
        }
        category.setName(request.getName().trim());
        category.setType(request.getType());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(Long userId, Long categoryId) {
        TransactionCategory category = findOwnedCustomCategory(userId, categoryId);
        category.setActive(false);
        categoryRepository.save(category);
    }

    private TransactionCategory findOwnedCustomCategory(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserIdAndSystemDefinedFalse(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría personalizada no encontrada."
                ));
    }

    private void ensureUniqueName(Long userId, CategoryType type, String name, Long excludedId) {
        if (categoryRepository.existsAvailableName(userId, type, name.trim(), excludedId)) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre y tipo.");
        }
    }
}

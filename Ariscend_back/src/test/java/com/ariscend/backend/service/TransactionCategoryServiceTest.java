package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.UpdateCategoryRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.TransactionCategory;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import com.ariscend.backend.repository.TransactionCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTest {
    @Mock TransactionCategoryRepository categoryRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock FinancialTransactionRepository transactionRepository;
    @InjectMocks TransactionCategoryService service;

    @Test
    void updateDoesNotModifySystemOrForeignCategory() {
        when(categoryRepository.findByIdAndUserIdAndSystemDefinedFalse(4L, 2L)).thenReturn(Optional.empty());
        UpdateCategoryRequest request = request(CategoryType.EXPENSE);

        assertThrows(ResourceNotFoundException.class, () -> service.update(2L, 4L, request));

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateDoesNotChangeTypeOfUsedCategory() {
        AppUser user = new AppUser(); user.setId(1L);
        TransactionCategory category = new TransactionCategory(); category.setId(4L); category.setUser(user);
        category.setName("Personal"); category.setType(CategoryType.EXPENSE);
        when(categoryRepository.findByIdAndUserIdAndSystemDefinedFalse(4L, 1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsAvailableName(1L, CategoryType.INCOME, "Personal", 4L)).thenReturn(false);
        when(transactionRepository.existsByCategoryId(4L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.update(1L, 4L, request(CategoryType.INCOME)));

        verify(categoryRepository, never()).save(any());
    }

    private UpdateCategoryRequest request(CategoryType type) {
        UpdateCategoryRequest request = new UpdateCategoryRequest(); request.setName("Personal"); request.setType(type); return request;
    }
}

package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CreateGoalContributionRequest;
import com.ariscend.backend.dto.finance.GoalContributionResponse;
import com.ariscend.backend.entity.*;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialGoalServiceTest {
    @Mock FinancialGoalRepository goalRepository;
    @Mock GoalContributionRepository contributionRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock TransactionCategoryRepository categoryRepository;
    @Mock CardRepository cardRepository;
    @Mock FinancialTransactionService transactionService;
    @Mock FinanceSettingsService settingsService;
    @InjectMocks FinancialGoalService service;

    @Test
    void contributionCreatesLinkedExpenseAndCompletesGoalAtomically() {
        AppUser user = new AppUser(); user.setId(1L); FinancialGoal goal = goal(user);
        TransactionCategory category = new TransactionCategory(); category.setId(3L);
        FinanceSettings settings = new FinanceSettings(); settings.setUser(user); settings.setBaseCurrency("MXN");
        FinancialTransaction transaction = new FinancialTransaction(); transaction.setId(8L); transaction.setUser(user);
        when(goalRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(goal));
        when(settingsService.getOrCreateEntity(1L)).thenReturn(settings);
        when(categoryRepository.findBySystemKey("EXPENSE_GOALS")).thenReturn(Optional.of(category));
        when(transactionService.createGoalExpense(eq(user), eq(category), isNull(), any(), eq("MXN"), any(), any(), anyString())).thenReturn(transaction);
        when(contributionRepository.save(any(GoalContribution.class))).thenAnswer(invocation -> {
            GoalContribution contribution = invocation.getArgument(0); contribution.setId(9L); contribution.assignCreatedAt(); return contribution;
        });
        when(contributionRepository.sumActiveByGoalId(2L)).thenReturn(new BigDecimal("1000.00"));
        CreateGoalContributionRequest request = new CreateGoalContributionRequest(); request.setAmount(new BigDecimal("1000.00"));
        request.setContributionDate(LocalDate.of(2026, 8, 14));

        GoalContributionResponse response = service.addContribution(1L, 2L, request);

        assertEquals(9L, response.getId()); assertEquals(8L, response.getTransactionId());
        assertEquals(GoalStatus.COMPLETED, goal.getStatus());
        verify(goalRepository).save(goal);
    }

    @Test
    void contributionDoesNotUseGoalOwnedByAnotherUser() {
        when(goalRepository.findByIdAndUserId(2L, 7L)).thenReturn(Optional.empty());
        CreateGoalContributionRequest request = new CreateGoalContributionRequest(); request.setAmount(BigDecimal.TEN);
        request.setContributionDate(LocalDate.now());

        assertThrows(ResourceNotFoundException.class, () -> service.addContribution(7L, 2L, request));

        verify(transactionService, never()).createGoalExpense(any(), any(), any(), any(), any(), any(), any(), any());
        verify(contributionRepository, never()).save(any());
    }

    private FinancialGoal goal(AppUser user) {
        FinancialGoal goal = new FinancialGoal(); goal.setId(2L); goal.setUser(user); goal.setName("Moto");
        goal.setTargetAmount(new BigDecimal("1000.00")); goal.setCurrency("MXN"); goal.setStatus(GoalStatus.ACTIVE); return goal;
    }
}

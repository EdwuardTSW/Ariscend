package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CreateFinancialGoalRequest;
import com.ariscend.backend.dto.finance.CreateGoalContributionRequest;
import com.ariscend.backend.dto.finance.FinancialGoalResponse;
import com.ariscend.backend.dto.finance.GoalContributionResponse;
import com.ariscend.backend.dto.finance.UpdateFinancialGoalRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Card;
import com.ariscend.backend.entity.CardType;
import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.FinancialGoal;
import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.FinancialTransaction;
import com.ariscend.backend.entity.GoalContribution;
import com.ariscend.backend.entity.GoalStatus;
import com.ariscend.backend.entity.TransactionCategory;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.CardRepository;
import com.ariscend.backend.repository.FinancialGoalRepository;
import com.ariscend.backend.repository.GoalContributionRepository;
import com.ariscend.backend.repository.TransactionCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FinancialGoalService {
    private final FinancialGoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final AppUserRepository appUserRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final FinancialTransactionService transactionService;
    private final FinanceSettingsService settingsService;

    public FinancialGoalService(FinancialGoalRepository goalRepository,
            GoalContributionRepository contributionRepository, AppUserRepository appUserRepository,
            TransactionCategoryRepository categoryRepository, CardRepository cardRepository,
            FinancialTransactionService transactionService, FinanceSettingsService settingsService) {
        this.goalRepository = goalRepository; this.contributionRepository = contributionRepository;
        this.appUserRepository = appUserRepository; this.categoryRepository = categoryRepository;
        this.cardRepository = cardRepository; this.transactionService = transactionService; this.settingsService = settingsService;
    }

    @Transactional
    public FinancialGoalResponse create(Long userId, CreateFinancialGoalRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        FinancialGoal goal = new FinancialGoal(); goal.setUser(user); apply(goal, request);
        goal = goalRepository.save(goal); return FinancialGoalResponse.from(goal, BigDecimal.ZERO);
    }

    public List<FinancialGoalResponse> getAll(Long userId, GoalStatus status) {
        if (!appUserRepository.existsById(userId)) throw new ResourceNotFoundException("Usuario no encontrado.");
        GoalStatus effectiveStatus = status == null ? GoalStatus.ACTIVE : status;
        return goalRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, effectiveStatus).stream()
                .map(goal -> FinancialGoalResponse.from(goal, currentAmount(goal.getId()))).toList();
    }

    public FinancialGoalResponse getById(Long userId, Long goalId) {
        FinancialGoal goal = findOwnedGoal(userId, goalId);
        return FinancialGoalResponse.from(goal, currentAmount(goalId));
    }

    @Transactional
    public FinancialGoalResponse update(Long userId, Long goalId, UpdateFinancialGoalRequest request) {
        FinancialGoal goal = findOwnedGoalForUpdate(userId, goalId);
        if (goal.getStatus() == GoalStatus.CANCELLED) throw new IllegalStateException("La meta está cancelada.");
        String newCurrency = FinanceUtils.normalizeCurrency(request.getCurrency());
        if (!goal.getCurrency().equals(newCurrency)
                && contributionRepository.existsByGoalIdAndStatus(goalId, FinancialStatus.ACTIVE)) {
            throw new IllegalStateException("No se puede cambiar la moneda de una meta con aportes.");
        }
        apply(goal, request); BigDecimal current = currentAmount(goalId);
        updateCompletionStatus(goal, current); return FinancialGoalResponse.from(goalRepository.saveAndFlush(goal), current);
    }

    @Transactional
    public void cancel(Long userId, Long goalId) {
        FinancialGoal goal = findOwnedGoalForUpdate(userId, goalId);
        if (goal.getStatus() != GoalStatus.CANCELLED) {
            goal.setStatus(GoalStatus.CANCELLED); goal.setCancelledAt(LocalDateTime.now()); goalRepository.save(goal);
        }
    }

    @Transactional
    public GoalContributionResponse addContribution(Long userId, Long goalId, CreateGoalContributionRequest request) {
        FinancialGoal goal = findOwnedGoalForUpdate(userId, goalId);
        if (goal.getStatus() != GoalStatus.ACTIVE) throw new IllegalStateException("La meta no está activa.");
        String baseCurrency = settingsService.getOrCreateEntity(userId).getBaseCurrency();
        FinanceUtils.calculateBaseAmount(
                request.getAmount(), goal.getCurrency(), baseCurrency, request.getExchangeRate()
        );
        BigDecimal normalizedRate = FinanceUtils.normalizedExchangeRate(goal.getCurrency(), baseCurrency, request.getExchangeRate());
        TransactionCategory category = categoryRepository.findBySystemKey("EXPENSE_GOALS")
                .orElseThrow(() -> new IllegalStateException("La categoría de metas no está disponible."));
        Card sourceCard = null;
        if (request.getSourceDebitCardId() != null) {
            sourceCard = cardRepository.findByIdAndUserId(request.getSourceDebitCardId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
            if (!sourceCard.isActive() || sourceCard.getCancelledAt() != null || sourceCard.getType() != CardType.DEBIT)
                throw new IllegalArgumentException("La tarjeta origen debe ser una tarjeta de débito activa.");
            if (!sourceCard.getCurrency().equals(goal.getCurrency()))
                throw new IllegalArgumentException("La moneda de la tarjeta no coincide con la meta.");
        }
        FinancialTransaction transaction = transactionService.createGoalExpense(goal.getUser(), category, sourceCard,
                request.getAmount(), goal.getCurrency(), normalizedRate, request.getContributionDate(), "Aporte a meta: " + goal.getName());
        GoalContribution contribution = new GoalContribution(); contribution.setGoal(goal); contribution.setAmount(request.getAmount());
        contribution.setExchangeRate(normalizedRate); contribution.setContributionDate(request.getContributionDate());
        contribution.setNotes(request.getNotes()); contribution.setLinkedTransaction(transaction);
        contribution = contributionRepository.save(contribution);
        BigDecimal current = currentAmount(goalId);
        updateCompletionStatus(goal, current); goalRepository.save(goal);
        return GoalContributionResponse.from(contribution);
    }

    public List<GoalContributionResponse> getContributions(Long userId, Long goalId) {
        findOwnedGoal(userId, goalId);
        return contributionRepository.findByGoalIdOrderByContributionDateDescCreatedAtDesc(goalId).stream()
                .map(GoalContributionResponse::from).toList();
    }

    @Transactional
    public void cancelContribution(Long userId, Long goalId, Long contributionId) {
        FinancialGoal goal = findOwnedGoalForUpdate(userId, goalId);
        GoalContribution contribution = contributionRepository.findOwned(contributionId, goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aporte no encontrado."));
        if (contribution.getStatus() != FinancialStatus.CANCELLED) {
            contribution.setStatus(FinancialStatus.CANCELLED); contribution.setCancelledAt(LocalDateTime.now());
            transactionService.cancelGoalExpense(contribution.getLinkedTransaction()); contributionRepository.save(contribution);
            if (goal.getStatus() != GoalStatus.CANCELLED) {
                BigDecimal current = currentAmount(goalId); updateCompletionStatus(goal, current); goalRepository.save(goal);
            }
        }
    }

    private void apply(FinancialGoal goal, CreateFinancialGoalRequest request) {
        goal.setName(request.getName().trim()); goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount()); goal.setCurrency(FinanceUtils.normalizeCurrency(request.getCurrency()));
        goal.setTargetDate(request.getTargetDate());
    }
    private FinancialGoal findOwnedGoal(Long userId, Long goalId) {
        return goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financiera no encontrada."));
    }
    private FinancialGoal findOwnedGoalForUpdate(Long userId, Long goalId) {
        return goalRepository.findOwnedForUpdate(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financiera no encontrada."));
    }
    private BigDecimal currentAmount(Long goalId) {
        BigDecimal amount = contributionRepository.sumActiveByGoalId(goalId); return amount == null ? BigDecimal.ZERO : amount;
    }
    private void updateCompletionStatus(FinancialGoal goal, BigDecimal current) {
        if (current.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(GoalStatus.COMPLETED); if (goal.getCompletedAt() == null) goal.setCompletedAt(LocalDateTime.now());
        } else { goal.setStatus(GoalStatus.ACTIVE); goal.setCompletedAt(null); }
    }
}

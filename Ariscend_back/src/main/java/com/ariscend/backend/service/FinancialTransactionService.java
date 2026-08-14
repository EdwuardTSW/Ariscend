package com.ariscend.backend.service;

import com.ariscend.backend.dto.common.PagedResponse;
import com.ariscend.backend.dto.finance.CreateFinancialTransactionRequest;
import com.ariscend.backend.dto.finance.CurrencyTotalsResponse;
import com.ariscend.backend.dto.finance.FinanceSummaryResponse;
import com.ariscend.backend.dto.finance.FinancialTransactionResponse;
import com.ariscend.backend.dto.finance.UpdateFinancialTransactionRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Card;
import com.ariscend.backend.entity.CardType;
import com.ariscend.backend.entity.CategoryType;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.entity.FinancialStatus;
import com.ariscend.backend.entity.FinancialTransaction;
import com.ariscend.backend.entity.TransactionCategory;
import com.ariscend.backend.entity.TransactionType;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.CardRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import com.ariscend.backend.repository.TransactionCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@Transactional(readOnly = true)
public class FinancialTransactionService {
    private final FinancialTransactionRepository transactionRepository;
    private final AppUserRepository appUserRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final CardRepository cardRepository;
    private final FinanceSettingsService settingsService;

    public FinancialTransactionService(FinancialTransactionRepository transactionRepository,
            AppUserRepository appUserRepository, TransactionCategoryRepository categoryRepository,
            CardRepository cardRepository, FinanceSettingsService settingsService) {
        this.transactionRepository = transactionRepository; this.appUserRepository = appUserRepository;
        this.categoryRepository = categoryRepository; this.cardRepository = cardRepository; this.settingsService = settingsService;
    }

    @Transactional
    public FinancialTransactionResponse create(Long userId, CreateFinancialTransactionRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        FinancialTransaction transaction = new FinancialTransaction(); transaction.setUser(user);
        apply(transaction, userId, request); validateAvailableFunds(transaction, null, null);
        return FinancialTransactionResponse.from(transactionRepository.save(transaction));
    }

    public PagedResponse<FinancialTransactionResponse> getAll(Long userId, TransactionType type,
            Long categoryId, Long cardId, String currency, LocalDate dateFrom, LocalDate dateTo,
            FinancialStatus status, int page, int size) {
        if (!appUserRepository.existsById(userId)) throw new ResourceNotFoundException("Usuario no encontrado.");
        validateDates(dateFrom, dateTo); validatePagination(page, size);
        String normalizedCurrency = currency == null || currency.isBlank() ? null : FinanceUtils.normalizeCurrency(currency);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("id")));
        Specification<FinancialTransaction> filters = (root, query, builder) ->
                builder.equal(root.get("user").get("id"), userId);
        if (type != null) filters = filters.and((root, query, builder) -> builder.equal(root.get("type"), type));
        if (categoryId != null) filters = filters.and((root, query, builder) ->
                builder.equal(root.get("category").get("id"), categoryId));
        if (cardId != null) filters = filters.and((root, query, builder) -> builder.or(
                builder.equal(root.get("card").get("id"), cardId),
                builder.equal(root.get("paidCreditCard").get("id"), cardId)
        ));
        if (normalizedCurrency != null) filters = filters.and((root, query, builder) ->
                builder.equal(root.get("currency"), normalizedCurrency));
        if (dateFrom != null) filters = filters.and((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("transactionDate"), dateFrom));
        if (dateTo != null) filters = filters.and((root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("transactionDate"), dateTo));
        if (status != null) filters = filters.and((root, query, builder) -> builder.equal(root.get("status"), status));
        Page<FinancialTransactionResponse> result = transactionRepository.findAll(filters, pageable)
                .map(FinancialTransactionResponse::from);
        return PagedResponse.from(result);
    }

    public FinancialTransactionResponse getById(Long userId, Long transactionId) {
        return FinancialTransactionResponse.from(findOwned(userId, transactionId));
    }

    @Transactional
    public FinancialTransactionResponse update(Long userId, Long transactionId, UpdateFinancialTransactionRequest request) {
        FinancialTransaction transaction = findOwnedForUpdate(userId, transactionId);
        if (transaction.getStatus() == FinancialStatus.CANCELLED) throw new IllegalStateException("El movimiento está cancelado.");
        if (transaction.isGoalGenerated()) throw new IllegalStateException("Los movimientos de metas se administran desde la meta.");
        TransactionImpact previousImpact = TransactionImpact.from(transaction);
        FundsSnapshot fundsSnapshot = captureFundsBeforeUpdate(userId, request, transaction);
        apply(transaction, userId, request); validateAvailableFunds(transaction, previousImpact, fundsSnapshot);
        return FinancialTransactionResponse.from(transactionRepository.saveAndFlush(transaction));
    }

    @Transactional
    public void cancel(Long userId, Long transactionId) {
        FinancialTransaction transaction = findOwnedForUpdate(userId, transactionId);
        if (transaction.isGoalGenerated()) throw new IllegalStateException("Los movimientos de metas se administran desde la meta.");
        if (transaction.getStatus() == FinancialStatus.ACTIVE) validateCancellation(userId, transaction);
        cancelEntity(transaction);
    }

    public FinanceSummaryResponse getSummary(Long userId, LocalDate dateFrom, LocalDate dateTo) {
        if (!appUserRepository.existsById(userId)) throw new ResourceNotFoundException("Usuario no encontrado.");
        if (dateFrom == null || dateTo == null) throw new IllegalArgumentException("El período del resumen es obligatorio.");
        validateDates(dateFrom, dateTo);
        BigDecimal income = BigDecimal.ZERO; BigDecimal expenses = BigDecimal.ZERO;
        for (Object[] row : transactionRepository.summarizeByType(userId, dateFrom, dateTo)) {
            if (row[0] == TransactionType.INCOME) income = (BigDecimal) row[1];
            if (row[0] == TransactionType.EXPENSE) expenses = (BigDecimal) row[1];
        }
        FinanceSummaryResponse response = new FinanceSummaryResponse();
        response.setBaseCurrency(settingsService.getOrCreateEntity(userId).getBaseCurrency());
        response.setDateFrom(dateFrom); response.setDateTo(dateTo); response.setTotalIncome(income);
        response.setTotalExpenses(expenses); response.setBalance(income.subtract(expenses));
        response.setIncomeByCategory(categoryTotals(userId, TransactionType.INCOME, dateFrom, dateTo));
        response.setExpensesByCategory(categoryTotals(userId, TransactionType.EXPENSE, dateFrom, dateTo));
        Map<String, CurrencyTotalsResponse> currencies = new LinkedHashMap<>();
        for (Object[] row : transactionRepository.summarizeOriginalByCurrency(userId, dateFrom, dateTo)) {
            String code = (String) row[0]; TransactionType rowType = (TransactionType) row[1]; BigDecimal amount = (BigDecimal) row[2];
            CurrencyTotalsResponse totals = currencies.computeIfAbsent(code, key -> new CurrencyTotalsResponse());
            if (rowType == TransactionType.INCOME) totals.setIncome(amount); else totals.setExpenses(amount);
        }
        response.setOriginalTotalsByCurrency(currencies); return response;
    }

    @Transactional
    public FinancialTransaction createGoalExpense(AppUser user, TransactionCategory category, Card sourceCard,
            BigDecimal amount, String currency, BigDecimal exchangeRate, LocalDate date, String description) {
        FinanceSettings settings = settingsService.getOrCreateEntity(user.getId());
        if (sourceCard != null) {
            sourceCard = findActiveCard(user.getId(), sourceCard.getId());
        }
        FinancialTransaction transaction = new FinancialTransaction(); transaction.setUser(user);
        transaction.setType(TransactionType.EXPENSE); transaction.setCategory(category); transaction.setCard(sourceCard);
        transaction.setAmount(amount); transaction.setCurrency(currency);
        transaction.setBaseAmount(FinanceUtils.calculateBaseAmount(amount, currency, settings.getBaseCurrency(), exchangeRate));
        transaction.setExchangeRate(FinanceUtils.normalizedExchangeRate(currency, settings.getBaseCurrency(), exchangeRate));
        transaction.setTransactionDate(date); transaction.setDescription(description); transaction.setGoalGenerated(true);
        validateAvailableFunds(transaction, null, null);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void cancelGoalExpense(FinancialTransaction transaction) { cancelEntity(transaction); }

    private void apply(FinancialTransaction transaction, Long userId, CreateFinancialTransactionRequest request) {
        FinanceSettings settings = settingsService.getOrCreateEntity(userId);
        String currency = FinanceUtils.normalizeCurrency(request.getCurrency());
        transaction.setType(request.getType()); transaction.setAmount(request.getAmount()); transaction.setCurrency(currency);
        transaction.setBaseAmount(FinanceUtils.calculateBaseAmount(request.getAmount(), currency, settings.getBaseCurrency(), request.getExchangeRate()));
        transaction.setExchangeRate(FinanceUtils.normalizedExchangeRate(currency, settings.getBaseCurrency(), request.getExchangeRate()));
        transaction.setDescription(request.getDescription()); transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCategory(null); transaction.setCard(null); transaction.setPaidCreditCard(null);
        if (request.getType() == TransactionType.CREDIT_CARD_PAYMENT) applyCardPayment(transaction, userId, request, currency);
        else applyIncomeOrExpense(transaction, userId, request, currency);
    }

    private void applyIncomeOrExpense(FinancialTransaction transaction, Long userId,
            CreateFinancialTransactionRequest request, String currency) {
        if (request.getCategoryId() == null) throw new IllegalArgumentException("La categoría es obligatoria.");
        TransactionCategory category = categoryRepository.findAvailableById(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada."));
        CategoryType expected = request.getType() == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        if (category.getType() != expected) throw new IllegalArgumentException("La categoría no coincide con el tipo de movimiento.");
        if (request.getPaidCreditCardId() != null) throw new IllegalArgumentException("Sólo un pago puede indicar una tarjeta de crédito destino.");
        transaction.setCategory(category);
        if (request.getCardId() != null) {
            Card card = findActiveCard(userId, request.getCardId()); ensureCurrency(card, currency);
            if (request.getType() == TransactionType.INCOME && card.getType() != CardType.DEBIT)
                throw new IllegalArgumentException("Un ingreso sólo puede depositarse en una tarjeta de débito.");
            transaction.setCard(card);
        }
    }

    private void applyCardPayment(FinancialTransaction transaction, Long userId,
            CreateFinancialTransactionRequest request, String currency) {
        if (request.getCategoryId() != null) throw new IllegalArgumentException("Un pago de tarjeta no utiliza categoría.");
        if (request.getPaidCreditCardId() == null) throw new IllegalArgumentException("La tarjeta de crédito destino es obligatoria.");
        Card target = findPaymentTargetCard(userId, request.getPaidCreditCardId()); ensureCurrency(target, currency);
        if (target.getType() != CardType.CREDIT) throw new IllegalArgumentException("La tarjeta destino debe ser de crédito.");
        transaction.setPaidCreditCard(target);
        if (request.getCardId() != null) {
            Card source = findActiveCard(userId, request.getCardId()); ensureCurrency(source, currency);
            if (source.getType() != CardType.DEBIT) throw new IllegalArgumentException("La tarjeta origen debe ser de débito.");
            transaction.setCard(source);
        }
    }

    private Card findActiveCard(Long userId, Long cardId) {
        Card card = cardRepository.findOwnedForUpdate(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
        if (!card.isActive() || card.getCancelledAt() != null) throw new IllegalStateException("La tarjeta no está activa.");
        return card;
    }
    private Card findPaymentTargetCard(Long userId, Long cardId) {
        Card card = cardRepository.findOwnedForUpdate(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
        if (card.getCancelledAt() != null) throw new IllegalStateException("La tarjeta está cancelada.");
        return card;
    }
    private void ensureCurrency(Card card, String currency) {
        if (!card.getCurrency().equals(currency)) throw new IllegalArgumentException("La moneda del movimiento no coincide con la tarjeta.");
    }
    private FinancialTransaction findOwned(Long userId, Long transactionId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado."));
    }
    private FinancialTransaction findOwnedForUpdate(Long userId, Long transactionId) {
        return transactionRepository.findOwnedForUpdate(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado."));
    }
    private void cancelEntity(FinancialTransaction transaction) {
        if (transaction.getStatus() != FinancialStatus.CANCELLED) {
            transaction.setStatus(FinancialStatus.CANCELLED); transaction.setCancelledAt(LocalDateTime.now()); transactionRepository.save(transaction);
        }
    }
    private Map<String, BigDecimal> categoryTotals(Long userId, TransactionType type, LocalDate from, LocalDate to) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Object[] row : transactionRepository.summarizeByCategory(userId, type, from, to)) totals.put((String) row[0], (BigDecimal) row[1]);
        return totals;
    }
    private void validateDates(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
    }
    private void validatePagination(int page, int size) {
        if (page < 0) throw new IllegalArgumentException("La página no puede ser negativa.");
        if (size < 1 || size > 100) throw new IllegalArgumentException("El tamaño de página debe estar entre 1 y 100.");
    }

    private void validateAvailableFunds(
            FinancialTransaction transaction,
            TransactionImpact previous,
            FundsSnapshot snapshot
    ) {
        Card card = transaction.getCard();
        if (snapshot != null) {
            Map<Long, BigDecimal> projectedBalances = new HashMap<>(snapshot.debitBalances());
            if (previous != null && previous.cardId() != null && projectedBalances.containsKey(previous.cardId())) {
                BigDecimal balance = projectedBalances.get(previous.cardId());
                balance = previous.type() == TransactionType.INCOME
                        ? balance.subtract(previous.amount())
                        : balance.add(previous.amount());
                projectedBalances.put(previous.cardId(), balance);
            }
            if (card != null && card.getType() == CardType.DEBIT) {
                BigDecimal balance = projectedBalances.getOrDefault(card.getId(), BigDecimal.ZERO);
                balance = transaction.getType() == TransactionType.INCOME
                        ? balance.add(transaction.getAmount())
                        : balance.subtract(transaction.getAmount());
                projectedBalances.put(card.getId(), balance);
            }
            if (projectedBalances.values().stream().anyMatch(balance -> balance.signum() < 0)) {
                throw new IllegalStateException("La operación dejaría una tarjeta de débito con saldo negativo.");
            }
            Map<Long, BigDecimal> projectedExpenses = new HashMap<>(snapshot.creditExpenses());
            Map<Long, BigDecimal> projectedPayments = new HashMap<>(snapshot.creditPayments());
            if (previous != null && previous.type() == TransactionType.EXPENSE
                    && previous.cardId() != null && projectedExpenses.containsKey(previous.cardId())) {
                projectedExpenses.computeIfPresent(previous.cardId(), (id, amount) -> amount.subtract(previous.amount()));
            }
            if (previous != null && previous.type() == TransactionType.CREDIT_CARD_PAYMENT
                    && previous.paidCreditCardId() != null && projectedPayments.containsKey(previous.paidCreditCardId())) {
                projectedPayments.computeIfPresent(previous.paidCreditCardId(), (id, amount) -> amount.subtract(previous.amount()));
            }
            if (card != null && card.getType() == CardType.CREDIT && transaction.getType() == TransactionType.EXPENSE) {
                projectedExpenses.merge(card.getId(), transaction.getAmount(), BigDecimal::add);
            }
            Card target = transaction.getPaidCreditCard();
            if (target != null) projectedPayments.merge(target.getId(), transaction.getAmount(), BigDecimal::add);
            validateProjectedCredit(projectedExpenses, projectedPayments, snapshot.creditLimits());
        } else if (card != null && card.getType() == CardType.DEBIT
                && transaction.getType() != TransactionType.INCOME
                && debitBalance(card).compareTo(transaction.getAmount()) < 0) {
            throw new IllegalStateException("La tarjeta de débito no tiene saldo suficiente.");
        }
        if (snapshot == null && card != null && card.getType() == CardType.CREDIT && transaction.getType() == TransactionType.EXPENSE) {
            BigDecimal debt = snapshot == null ? creditDebt(card.getId()) : snapshot.creditDebt(card.getId());
            if (previous != null && previous.type() == TransactionType.EXPENSE
                    && card.getId().equals(previous.cardId())) debt = debt.subtract(previous.amount()).max(BigDecimal.ZERO);
            if (debt.add(transaction.getAmount()).compareTo(card.getCreditLimit()) > 0) {
                throw new IllegalStateException("La compra supera el crédito disponible.");
            }
        }
        Card target = transaction.getPaidCreditCard();
        if (snapshot == null && target != null) {
            BigDecimal debt = snapshot == null ? creditDebt(target.getId()) : snapshot.creditDebt(target.getId());
            if (previous != null && previous.type() == TransactionType.CREDIT_CARD_PAYMENT
                    && target.getId().equals(previous.paidCreditCardId())) debt = debt.add(previous.amount());
            if (transaction.getAmount().compareTo(debt) > 0) {
                throw new IllegalStateException("El pago no puede superar la deuda actual.");
            }
        }
    }

    private BigDecimal debitBalance(Card card) {
        BigDecimal opening = card.getOpeningBalance() == null ? BigDecimal.ZERO : card.getOpeningBalance();
        BigDecimal income = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.INCOME));
        BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.EXPENSE));
        BigDecimal payments = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.CREDIT_CARD_PAYMENT));
        return opening.add(income).subtract(expenses).subtract(payments);
    }

    private BigDecimal creditDebt(Long cardId) {
        BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(cardId, TransactionType.EXPENSE));
        BigDecimal payments = value(transactionRepository.sumActivePaymentsToCard(cardId));
        return expenses.subtract(payments).max(BigDecimal.ZERO);
    }

    private BigDecimal value(BigDecimal amount) { return amount == null ? BigDecimal.ZERO : amount; }

    private FundsSnapshot captureFundsBeforeUpdate(
            Long userId,
            CreateFinancialTransactionRequest request,
            FinancialTransaction current
    ) {
        Map<Long, BigDecimal> debitBalances = new HashMap<>();
        Map<Long, BigDecimal> creditExpenses = new HashMap<>();
        Map<Long, BigDecimal> creditPayments = new HashMap<>();
        Map<Long, BigDecimal> creditLimits = new HashMap<>();
        Set<Long> cardIds = new TreeSet<>();
        if (current.getCard() != null) cardIds.add(current.getCard().getId());
        if (current.getPaidCreditCard() != null) cardIds.add(current.getPaidCreditCard().getId());
        if (request.getCardId() != null) cardIds.add(request.getCardId());
        if (request.getPaidCreditCardId() != null) cardIds.add(request.getPaidCreditCardId());
        for (Long cardId : cardIds) {
            Card card = cardRepository.findOwnedForUpdate(cardId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
            captureCardFunds(card, debitBalances, creditExpenses, creditPayments, creditLimits);
        }
        return new FundsSnapshot(debitBalances, creditExpenses, creditPayments, creditLimits);
    }

    private void captureCardFunds(
            Card card,
            Map<Long, BigDecimal> balances,
            Map<Long, BigDecimal> expenses,
            Map<Long, BigDecimal> payments,
            Map<Long, BigDecimal> limits
    ) {
        if (card == null || balances.containsKey(card.getId())) return;
        if (card.getType() == CardType.DEBIT) {
            balances.put(card.getId(), debitBalance(card));
            return;
        }
        balances.put(card.getId(), BigDecimal.ZERO);
        expenses.put(card.getId(), value(transactionRepository.sumActiveByCardAndType(
                card.getId(), TransactionType.EXPENSE
        )));
        payments.put(card.getId(), value(transactionRepository.sumActivePaymentsToCard(card.getId())));
        limits.put(card.getId(), card.getCreditLimit());
    }

    private record TransactionImpact(
            TransactionType type,
            Long cardId,
            Long paidCreditCardId,
            BigDecimal amount
    ) {
        static TransactionImpact from(FinancialTransaction transaction) {
            Long cardId = transaction.getCard() == null ? null : transaction.getCard().getId();
            Long paidCardId = transaction.getPaidCreditCard() == null ? null : transaction.getPaidCreditCard().getId();
            return new TransactionImpact(transaction.getType(), cardId, paidCardId, transaction.getAmount());
        }
    }

    private record FundsSnapshot(
            Map<Long, BigDecimal> debitBalances,
            Map<Long, BigDecimal> creditExpenses,
            Map<Long, BigDecimal> creditPayments,
            Map<Long, BigDecimal> creditLimits
    ) {
        BigDecimal debitBalance(Card card) {
            return debitBalances.getOrDefault(card.getId(), BigDecimal.ZERO);
        }

        BigDecimal creditDebt(Long cardId) {
            return creditExpenses.getOrDefault(cardId, BigDecimal.ZERO)
                    .subtract(creditPayments.getOrDefault(cardId, BigDecimal.ZERO))
                    .max(BigDecimal.ZERO);
        }
    }

    private void validateCancellation(Long userId, FinancialTransaction transaction) {
        if (transaction.getType() == TransactionType.INCOME && transaction.getCard() != null) {
            Card card = cardRepository.findOwnedForUpdate(transaction.getCard().getId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
            if (debitBalance(card).subtract(transaction.getAmount()).signum() < 0) {
                throw new IllegalStateException("No se puede cancelar el ingreso porque dejaría un saldo negativo.");
            }
        }
        if (transaction.getType() == TransactionType.EXPENSE && transaction.getCard() != null
                && transaction.getCard().getType() == CardType.CREDIT) {
            Card card = cardRepository.findOwnedForUpdate(transaction.getCard().getId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
            BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.EXPENSE))
                    .subtract(transaction.getAmount());
            BigDecimal payments = value(transactionRepository.sumActivePaymentsToCard(card.getId()));
            validateProjectedCredit(
                    Map.of(card.getId(), expenses),
                    Map.of(card.getId(), payments),
                    Map.of(card.getId(), card.getCreditLimit())
            );
        }
        if (transaction.getType() == TransactionType.CREDIT_CARD_PAYMENT) {
            Card card = cardRepository.findOwnedForUpdate(transaction.getPaidCreditCard().getId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
            BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.EXPENSE));
            BigDecimal payments = value(transactionRepository.sumActivePaymentsToCard(card.getId()))
                    .subtract(transaction.getAmount());
            validateProjectedCredit(
                    Map.of(card.getId(), expenses),
                    Map.of(card.getId(), payments),
                    Map.of(card.getId(), card.getCreditLimit())
            );
        }
    }

    private void validateProjectedCredit(
            Map<Long, BigDecimal> expenses,
            Map<Long, BigDecimal> payments,
            Map<Long, BigDecimal> limits
    ) {
        for (Long cardId : limits.keySet()) {
            BigDecimal cardExpenses = expenses.getOrDefault(cardId, BigDecimal.ZERO);
            BigDecimal cardPayments = payments.getOrDefault(cardId, BigDecimal.ZERO);
            if (cardPayments.compareTo(cardExpenses) > 0) {
                throw new IllegalStateException("La operación dejaría pagos superiores a la deuda registrada.");
            }
            if (cardExpenses.subtract(cardPayments).compareTo(limits.get(cardId)) > 0) {
                throw new IllegalStateException("La operación haría que la deuda supere el límite de crédito.");
            }
        }
    }
}

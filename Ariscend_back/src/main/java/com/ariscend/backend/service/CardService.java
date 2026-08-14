package com.ariscend.backend.service;

import com.ariscend.backend.dto.finance.CardResponse;
import com.ariscend.backend.dto.finance.CardSummaryResponse;
import com.ariscend.backend.dto.finance.CreateCardRequest;
import com.ariscend.backend.dto.finance.UpdateCardActiveRequest;
import com.ariscend.backend.dto.finance.UpdateCardRequest;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.entity.Card;
import com.ariscend.backend.entity.CardType;
import com.ariscend.backend.entity.FinanceSettings;
import com.ariscend.backend.entity.PaymentAlertStatus;
import com.ariscend.backend.entity.TransactionType;
import com.ariscend.backend.exception.ResourceNotFoundException;
import com.ariscend.backend.repository.AppUserRepository;
import com.ariscend.backend.repository.CardRepository;
import com.ariscend.backend.repository.FinancialTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CardService {
    private final CardRepository cardRepository;
    private final AppUserRepository appUserRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final FinanceSettingsService settingsService;

    public CardService(CardRepository cardRepository, AppUserRepository appUserRepository,
                       FinancialTransactionRepository transactionRepository, FinanceSettingsService settingsService) {
        this.cardRepository = cardRepository; this.appUserRepository = appUserRepository;
        this.transactionRepository = transactionRepository; this.settingsService = settingsService;
    }

    @Transactional
    public CardResponse create(Long userId, CreateCardRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));
        Card card = new Card(); card.setUser(user); apply(card, request);
        return CardResponse.from(cardRepository.save(card));
    }

    public List<CardResponse> getAll(Long userId) {
        if (!appUserRepository.existsById(userId)) throw new ResourceNotFoundException("Usuario no encontrado.");
        return cardRepository.findByUserIdOrderByActiveDescCreatedAtDesc(userId).stream().map(CardResponse::from).toList();
    }

    public CardResponse getById(Long userId, Long cardId) { return CardResponse.from(findOwnedCard(userId, cardId)); }

    @Transactional
    public CardResponse update(Long userId, Long cardId, UpdateCardRequest request) {
        Card card = findOwnedCardForUpdate(userId, cardId); ensureNotCancelled(card);
        if (transactionRepository.existsByCardIdOrPaidCreditCardId(cardId, cardId)
                && (card.getType() != request.getType() || !card.getCurrency().equalsIgnoreCase(request.getCurrency()))) {
            throw new IllegalStateException("No se puede cambiar el tipo o moneda de una tarjeta con movimientos.");
        }
        validateUpdatedBalance(card, request);
        apply(card, request); return CardResponse.from(cardRepository.saveAndFlush(card));
    }

    @Transactional
    public CardResponse updateActive(Long userId, Long cardId, UpdateCardActiveRequest request) {
        Card card = findOwnedCardForUpdate(userId, cardId); ensureNotCancelled(card);
        card.setActive(request.getActive()); return CardResponse.from(cardRepository.saveAndFlush(card));
    }

    @Transactional
    public void cancel(Long userId, Long cardId) {
        Card card = findOwnedCardForUpdate(userId, cardId);
        if (card.getType() == CardType.CREDIT) {
            BigDecimal debt = value(transactionRepository.sumActiveByCardAndType(cardId, TransactionType.EXPENSE))
                    .subtract(value(transactionRepository.sumActivePaymentsToCard(cardId)))
                    .max(BigDecimal.ZERO);
            if (debt.signum() > 0) {
                throw new IllegalStateException("No se puede cancelar una tarjeta de crédito con deuda.");
            }
        }
        if (card.getCancelledAt() == null) { card.setActive(false); card.setCancelledAt(LocalDateTime.now()); cardRepository.save(card); }
    }

    public CardSummaryResponse getSummary(Long userId, Long cardId) {
        Card card = findOwnedCard(userId, cardId);
        BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(cardId, TransactionType.EXPENSE));
        BigDecimal income = value(transactionRepository.sumActiveByCardAndType(cardId, TransactionType.INCOME));
        BigDecimal sourcePayments = value(transactionRepository.sumActiveByCardAndType(cardId, TransactionType.CREDIT_CARD_PAYMENT));
        BigDecimal receivedPayments = value(transactionRepository.sumActivePaymentsToCard(cardId));
        CardSummaryResponse response = new CardSummaryResponse();
        response.setCardId(cardId); response.setType(card.getType()); response.setCurrency(card.getCurrency());
        if (card.getType() == CardType.DEBIT) {
            response.setCurrentBalance(value(card.getOpeningBalance()).add(income).subtract(expenses).subtract(sourcePayments));
            response.setPaymentAlertStatus(PaymentAlertStatus.NONE); return response;
        }
        BigDecimal debt = expenses.subtract(receivedPayments).max(BigDecimal.ZERO);
        response.setCurrentDebt(debt); response.setAvailableCredit(card.getCreditLimit().subtract(debt));
        LocalDate today = LocalDate.now(); LocalDate closing = nextDate(today, card.getClosingDay());
        LocalDate payment = nextDate(today, card.getPaymentDueDay());
        long days = ChronoUnit.DAYS.between(today, payment);
        response.setNextClosingDate(closing); response.setNextPaymentDate(payment); response.setDaysUntilPayment(days);
        FinanceSettings settings = settingsService.getOrCreateEntity(userId);
        PaymentAlertStatus alert = PaymentAlertStatus.NONE;
        if (debt.signum() > 0 && days == 0) alert = PaymentAlertStatus.DUE_TODAY;
        else if (debt.signum() > 0 && days <= settings.getPaymentAlertDays()) alert = PaymentAlertStatus.UPCOMING;
        response.setPaymentAlertStatus(alert); return response;
    }

    public Card findOwnedCard(Long userId, Long cardId) {
        return cardRepository.findByIdAndUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
    }

    private Card findOwnedCardForUpdate(Long userId, Long cardId) {
        return cardRepository.findOwnedForUpdate(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada."));
    }

    private void apply(Card card, CreateCardRequest request) {
        String currency = FinanceUtils.normalizeCurrency(request.getCurrency());
        card.setAlias(request.getAlias().trim()); card.setIssuer(request.getIssuer().trim());
        card.setType(request.getType()); card.setLastFourDigits(request.getLastFourDigits()); card.setCurrency(currency);
        if (request.getType() == CardType.CREDIT) {
            if (request.getCreditLimit() == null || request.getClosingDay() == null || request.getPaymentDueDay() == null)
                throw new IllegalArgumentException("Una tarjeta de crédito requiere límite, día de corte y día de pago.");
            card.setCreditLimit(request.getCreditLimit()); card.setClosingDay(request.getClosingDay());
            card.setPaymentDueDay(request.getPaymentDueDay()); card.setOpeningBalance(null);
        } else {
            if (request.getOpeningBalance() == null) throw new IllegalArgumentException("Una tarjeta de débito requiere saldo inicial.");
            card.setOpeningBalance(request.getOpeningBalance()); card.setCreditLimit(null); card.setClosingDay(null); card.setPaymentDueDay(null);
        }
    }

    private void ensureNotCancelled(Card card) {
        if (card.getCancelledAt() != null) throw new IllegalStateException("La tarjeta está cancelada.");
    }
    private void validateUpdatedBalance(Card card, CreateCardRequest request) {
        BigDecimal expenses = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.EXPENSE));
        BigDecimal income = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.INCOME));
        BigDecimal sourcePayments = value(transactionRepository.sumActiveByCardAndType(card.getId(), TransactionType.CREDIT_CARD_PAYMENT));
        if (request.getType() == CardType.CREDIT) {
            BigDecimal debt = expenses.subtract(value(transactionRepository.sumActivePaymentsToCard(card.getId()))).max(BigDecimal.ZERO);
            if (request.getCreditLimit() != null && request.getCreditLimit().compareTo(debt) < 0)
                throw new IllegalStateException("El nuevo límite no puede ser menor que la deuda actual.");
        } else if (request.getOpeningBalance() != null) {
            BigDecimal projected = request.getOpeningBalance().add(income).subtract(expenses).subtract(sourcePayments);
            if (projected.signum() < 0) throw new IllegalStateException("El nuevo saldo inicial dejaría un saldo disponible negativo.");
        }
    }
    private BigDecimal value(BigDecimal amount) { return amount == null ? BigDecimal.ZERO : amount; }
    private LocalDate nextDate(LocalDate today, int day) {
        YearMonth month = YearMonth.from(today); LocalDate candidate = month.atDay(Math.min(day, month.lengthOfMonth()));
        if (candidate.isBefore(today)) { month = month.plusMonths(1); candidate = month.atDay(Math.min(day, month.lengthOfMonth())); }
        return candidate;
    }
}

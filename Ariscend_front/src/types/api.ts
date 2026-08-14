export interface User {
  id: number;
  name: string;
  email: string;
}

export interface Habit {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  frequency: "DAILY" | "WEEKLY";
  targetDaysPerWeek: number;
  color: string | null;
  icon: string | null;
  active: boolean;
  completedToday: boolean;
}

export interface HabitCompletion {
  id: number;
  habitId: number;
  completedDate: string;
  completedAt: string;
  notes: string | null;
}

export interface Note {
  id: number;
  userId: number;
  title: string | null;
  content: string;
  pinned: boolean;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Task {
  id: number;
  userId: number;
  title: string;
  description: string | null;
  completed: boolean;
  dueDate: string | null;
  priority: "LOW" | "MEDIUM" | "HIGH";
  createdAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiErrorBody {
  message: string;
}

export interface FinanceSettings {
  userId: number;
  baseCurrency: string;
  paymentAlertDays: number;
}

export interface FinanceSummary {
  baseCurrency: string;
  dateFrom: string;
  dateTo: string;
  totalIncome: number;
  totalExpenses: number;
  balance: number;
  incomeByCategory: Record<string, number>;
  expensesByCategory: Record<string, number>;
  originalTotalsByCurrency: Record<string, { income: number; expenses: number }>;
}

export type CategoryType = "INCOME" | "EXPENSE";
export type CardType = "CREDIT" | "DEBIT";
export type TransactionType = "INCOME" | "EXPENSE" | "CREDIT_CARD_PAYMENT";
export type FinancialStatus = "ACTIVE" | "CANCELLED";
export type GoalStatus = "ACTIVE" | "COMPLETED" | "CANCELLED";

export interface TransactionCategory {
  id: number;
  name: string;
  type: CategoryType;
  systemDefined: boolean;
  systemKey: string | null;
  active: boolean;
}

export interface Card {
  id: number;
  userId: number;
  alias: string;
  issuer: string;
  type: CardType;
  lastFourDigits: string;
  currency: string;
  creditLimit: number | null;
  openingBalance: number | null;
  closingDay: number | null;
  paymentDueDay: number | null;
  active: boolean;
  createdAt: string;
  cancelledAt: string | null;
}

export interface CardSummary {
  cardId: number;
  type: CardType;
  currency: string;
  currentBalance: number | null;
  currentDebt: number | null;
  availableCredit: number | null;
  nextClosingDate: string | null;
  nextPaymentDate: string | null;
  daysUntilPayment: number | null;
  paymentAlertStatus: "NONE" | "UPCOMING" | "DUE_TODAY";
}

export interface FinancialTransaction {
  id: number;
  userId: number;
  type: TransactionType;
  categoryId: number | null;
  categoryName: string | null;
  cardId: number | null;
  paidCreditCardId: number | null;
  amount: number;
  currency: string;
  exchangeRate: number;
  baseAmount: number;
  description: string | null;
  transactionDate: string;
  status: FinancialStatus;
  goalGenerated: boolean;
  createdAt: string;
  cancelledAt: string | null;
}

export interface FinancialGoal {
  id: number;
  userId: number;
  name: string;
  description: string | null;
  targetAmount: number;
  currentAmount: number;
  remainingAmount: number;
  progressPercentage: number;
  currency: string;
  targetDate: string | null;
  status: GoalStatus;
  createdAt: string;
}

export interface GoalContribution {
  id: number;
  goalId: number;
  transactionId: number;
  amount: number;
  currency: string;
  exchangeRate: number;
  contributionDate: string;
  notes: string | null;
  status: FinancialStatus;
  createdAt: string;
}

export interface CardInput {
  alias: string;
  issuer: string;
  type: CardType;
  lastFourDigits: string;
  currency: string;
  creditLimit?: number | null;
  openingBalance?: number | null;
  closingDay?: number | null;
  paymentDueDay?: number | null;
}

export interface FinancialTransactionInput {
  type: TransactionType;
  categoryId?: number | null;
  cardId?: number | null;
  paidCreditCardId?: number | null;
  amount: number;
  currency: string;
  exchangeRate?: number | null;
  description?: string | null;
  transactionDate: string;
}

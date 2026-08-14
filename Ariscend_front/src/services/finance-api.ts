import { apiRequest, toQueryString } from "@/services/api-client";
import type {
  Card,
  CardInput,
  CardSummary,
  CategoryType,
  FinanceSettings,
  FinanceSummary,
  FinancialGoal,
  FinancialStatus,
  FinancialTransaction,
  FinancialTransactionInput,
  GoalContribution,
  GoalStatus,
  PagedResponse,
  TransactionCategory,
  TransactionType,
} from "@/types/api";

export const financeApi = {
  settings: (userId: number) =>
    apiRequest<FinanceSettings>(`/api/users/${userId}/finance/settings`),
  updateSettings: (
    userId: number,
    data: { baseCurrency: string; paymentAlertDays: number },
  ) =>
    apiRequest<FinanceSettings>(`/api/users/${userId}/finance/settings`, {
      method: "PUT",
      body: JSON.stringify(data),
    }),
  summary: (userId: number, dateFrom: string, dateTo: string) =>
    apiRequest<FinanceSummary>(
      `/api/users/${userId}/finance/summary${toQueryString({ dateFrom, dateTo })}`,
    ),
  categories: (userId: number, type?: CategoryType) =>
    apiRequest<TransactionCategory[]>(
      `/api/users/${userId}/finance/categories${toQueryString({ type })}`,
    ),
  createCategory: (userId: number, data: { name: string; type: CategoryType }) =>
    apiRequest<TransactionCategory>(`/api/users/${userId}/finance/categories`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  deactivateCategory: (userId: number, categoryId: number) =>
    apiRequest<void>(`/api/users/${userId}/finance/categories/${categoryId}`, {
      method: "DELETE",
    }),
  transactions: (
    userId: number,
    filters: {
      type?: TransactionType;
      categoryId?: number;
      cardId?: number;
      currency?: string;
      dateFrom?: string;
      dateTo?: string;
      status?: FinancialStatus;
      page?: number;
      size?: number;
    } = {},
  ) =>
    apiRequest<PagedResponse<FinancialTransaction>>(
      `/api/users/${userId}/finance/transactions${toQueryString({ status: "ACTIVE", page: 0, size: 40, ...filters })}`,
    ),
  createTransaction: (userId: number, data: FinancialTransactionInput) =>
    apiRequest<FinancialTransaction>(`/api/users/${userId}/finance/transactions`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateTransaction: (
    userId: number,
    transactionId: number,
    data: FinancialTransactionInput,
  ) =>
    apiRequest<FinancialTransaction>(
      `/api/users/${userId}/finance/transactions/${transactionId}`,
      { method: "PUT", body: JSON.stringify(data) },
    ),
  cancelTransaction: (userId: number, transactionId: number) =>
    apiRequest<void>(`/api/users/${userId}/finance/transactions/${transactionId}`, {
      method: "DELETE",
    }),
  cards: (userId: number) =>
    apiRequest<Card[]>(`/api/users/${userId}/finance/cards`),
  createCard: (userId: number, data: CardInput) =>
    apiRequest<Card>(`/api/users/${userId}/finance/cards`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  updateCardActive: (userId: number, cardId: number, active: boolean) =>
    apiRequest<Card>(`/api/users/${userId}/finance/cards/${cardId}/active`, {
      method: "PATCH",
      body: JSON.stringify({ active }),
    }),
  cancelCard: (userId: number, cardId: number) =>
    apiRequest<void>(`/api/users/${userId}/finance/cards/${cardId}`, {
      method: "DELETE",
    }),
  cardSummary: (userId: number, cardId: number) =>
    apiRequest<CardSummary>(`/api/users/${userId}/finance/cards/${cardId}/summary`),
  goals: (userId: number, status: GoalStatus) =>
    apiRequest<FinancialGoal[]>(
      `/api/users/${userId}/finance/goals${toQueryString({ status })}`,
    ),
  createGoal: (
    userId: number,
    data: {
      name: string;
      description?: string | null;
      targetAmount: number;
      currency: string;
      targetDate?: string | null;
    },
  ) =>
    apiRequest<FinancialGoal>(`/api/users/${userId}/finance/goals`, {
      method: "POST",
      body: JSON.stringify(data),
    }),
  cancelGoal: (userId: number, goalId: number) =>
    apiRequest<void>(`/api/users/${userId}/finance/goals/${goalId}`, {
      method: "DELETE",
    }),
  contributions: (userId: number, goalId: number) =>
    apiRequest<GoalContribution[]>(
      `/api/users/${userId}/finance/goals/${goalId}/contributions`,
    ),
  addContribution: (
    userId: number,
    goalId: number,
    data: {
      amount: number;
      exchangeRate?: number | null;
      contributionDate: string;
      notes?: string | null;
      sourceDebitCardId?: number | null;
    },
  ) =>
    apiRequest<GoalContribution>(
      `/api/users/${userId}/finance/goals/${goalId}/contributions`,
      { method: "POST", body: JSON.stringify(data) },
    ),
  cancelContribution: (userId: number, goalId: number, contributionId: number) =>
    apiRequest<void>(
      `/api/users/${userId}/finance/goals/${goalId}/contributions/${contributionId}`,
      { method: "DELETE" },
    ),
};

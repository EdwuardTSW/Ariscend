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

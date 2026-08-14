import { apiRequest, toQueryString } from "@/services/api-client";
import type { FinanceSettings, FinanceSummary } from "@/types/api";

export const financeApi = {
  settings: (userId: number) =>
    apiRequest<FinanceSettings>(`/api/users/${userId}/finance/settings`),
  summary: (userId: number, dateFrom: string, dateTo: string) =>
    apiRequest<FinanceSummary>(
      `/api/users/${userId}/finance/summary${toQueryString({ dateFrom, dateTo })}`,
    ),
};

import { apiRequest, clearCsrfToken } from "@/services/api-client";
import type { ChangePasswordInput, LoginInput, RegisterInput, User } from "@/types/api";

export const authApi = {
  me: (signal?: AbortSignal) => apiRequest<User>("/api/auth/me", { signal }),
  providers: (signal?: AbortSignal) => apiRequest<{ google: boolean }>("/api/auth/providers", { signal }),
  login: (data: LoginInput) => apiRequest<User>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(data),
  }),
  register: (data: RegisterInput) => apiRequest<User>("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
  }),
  changePassword: (data: ChangePasswordInput) => apiRequest<void>("/api/auth/password", {
    method: "POST",
    body: JSON.stringify(data),
  }),
  logout: async () => {
    try {
      await apiRequest<void>("/api/auth/logout", { method: "POST" });
    } finally {
      clearCsrfToken();
    }
  },
};

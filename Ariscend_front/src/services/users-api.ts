import { apiRequest } from "@/services/api-client";
import type { User } from "@/types/api";

export const usersApi = {
  list: () => apiRequest<User[]>("/api/users"),
  create: (data: { name: string; email: string }) =>
    apiRequest<User>("/api/users", {
      method: "POST",
      body: JSON.stringify(data),
    }),
};

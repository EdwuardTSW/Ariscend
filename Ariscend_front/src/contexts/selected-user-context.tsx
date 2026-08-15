"use client";

import { useAuth } from "@/contexts/auth-context";

export function useSelectedUser() {
  const { user, loading, error, refresh } = useAuth();
  return { selectedUser: user, loading, error, refresh };
}

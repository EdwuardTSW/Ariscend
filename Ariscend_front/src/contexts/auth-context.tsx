"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { ApiError } from "@/services/api-client";
import { authApi } from "@/services/auth-api";
import type { LoginInput, RegisterInput, User } from "@/types/api";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (data: LoginInput) => Promise<User>;
  register: (data: RegisterInput) => Promise<User>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function refresh() {
    setLoading(true);
    setError(null);
    try {
      setUser(await authApi.me());
    } catch (requestError) {
      setUser(null);
      if (!(requestError instanceof ApiError && requestError.status === 401)) {
        setError(requestError instanceof Error ? requestError.message : "No se pudo comprobar tu sesión.");
      }
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let active = true;
    const controller = new AbortController();
    const handleUnauthorized = () => setUser(null);
    window.addEventListener("ariscend:unauthorized", handleUnauthorized);
    authApi.me(controller.signal)
      .then((authenticatedUser) => {
        if (active) setUser(authenticatedUser);
      })
      .catch((requestError) => {
        if (!active || controller.signal.aborted) return;
        setUser(null);
        if (!(requestError instanceof ApiError && requestError.status === 401)) {
          setError(requestError instanceof Error ? requestError.message : "No se pudo comprobar tu sesión.");
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
      controller.abort();
      window.removeEventListener("ariscend:unauthorized", handleUnauthorized);
    };
  }, []);

  async function login(data: LoginInput) {
    const authenticatedUser = await authApi.login(data);
    setUser(authenticatedUser);
    setError(null);
    return authenticatedUser;
  }

  async function register(data: RegisterInput) {
    const authenticatedUser = await authApi.register(data);
    setUser(authenticatedUser);
    setError(null);
    return authenticatedUser;
  }

  async function logout() {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, error, login, register, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth debe usarse dentro de AuthProvider.");
  return context;
}

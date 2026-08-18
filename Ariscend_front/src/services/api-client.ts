import type { ApiErrorBody } from "@/types/api";

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS", "TRACE"]);
let csrfToken: string | null = null;
let csrfRequest: Promise<string> | null = null;

export function clearCsrfToken() {
  csrfToken = null;
}

async function getCsrfToken() {
  if (csrfToken) return csrfToken;
  if (!csrfRequest) {
    csrfRequest = request<{ token: string }>("/api/auth/csrf", {}, true)
      .then(({ token }) => {
        csrfToken = token;
        return token;
      })
      .finally(() => {
        csrfRequest = null;
      });
  }
  return csrfRequest;
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  return request<T>(path, init, false);
}

async function request<T>(path: string, init: RequestInit, skipCsrf: boolean): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const method = (init.method ?? "GET").toUpperCase();
  if (!skipCsrf && !SAFE_METHODS.has(method)) {
    headers.set("X-XSRF-TOKEN", await getCsrfToken());
  }

  const controller = new AbortController();
  // Render Free can take close to a minute to wake after an idle period.
  const timeout = window.setTimeout(() => controller.abort(), 75_000);
  const abortFromCaller = () => controller.abort();
  init.signal?.addEventListener("abort", abortFromCaller, { once: true });

  let response: Response;
  try {
    response = await fetch(`/backend${path}`, {
      ...init,
      credentials: "same-origin",
      headers,
      signal: controller.signal,
    });
  } catch (error) {
    if (init.signal?.aborted) throw error;
    if (controller.signal.aborted) {
      throw new ApiError("La solicitud tardó demasiado. Revisa tu conexión e intenta nuevamente.", 408);
    }
    throw new ApiError("No se pudo conectar con el servidor.", 0);
  } finally {
    window.clearTimeout(timeout);
    init.signal?.removeEventListener("abort", abortFromCaller);
  }

  if (!response.ok) {
    if (response.status === 401 && typeof window !== "undefined") {
      window.dispatchEvent(new Event("ariscend:unauthorized"));
    }
    let message = response.status === 401
      ? "Tu sesión terminó. Inicia sesión nuevamente."
      : response.status >= 500
      ? "El servidor no pudo completar la solicitud. Intenta nuevamente."
      : "No se pudo completar la operación.";
    try {
      const body = (await response.json()) as ApiErrorBody;
      if (body.message && response.status < 500 && response.status !== 401) message = body.message;
    } catch {
      // Keep the fallback when the server did not return JSON.
    }
    throw new ApiError(message, response.status);
  }

  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}

export function toQueryString(values: Record<string, unknown>) {
  const query = new URLSearchParams();
  Object.entries(values).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  });
  const value = query.toString();
  return value ? `?${value}` : "";
}

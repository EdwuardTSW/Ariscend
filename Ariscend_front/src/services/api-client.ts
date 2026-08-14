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

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const controller = new AbortController();
  const timeout = window.setTimeout(() => controller.abort(), 12_000);
  const abortFromCaller = () => controller.abort();
  init.signal?.addEventListener("abort", abortFromCaller, { once: true });

  let response: Response;
  try {
    response = await fetch(`/backend${path}`, {
      ...init,
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
    let message = response.status >= 500
      ? "El servidor no pudo completar la solicitud. Intenta nuevamente."
      : "No se pudo completar la operación.";
    try {
      const body = (await response.json()) as ApiErrorBody;
      if (body.message && response.status < 500) message = body.message;
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

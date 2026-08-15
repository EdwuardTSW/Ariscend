"use client";

import { Suspense, useEffect, useState, type FormEvent } from "react";
import { ArrowRight, Eye, EyeOff, LockKeyhole } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/contexts/auth-context";
import { ApiError } from "@/services/api-client";
import { authApi } from "@/services/auth-api";

type Mode = "login" | "register";

function safeNextPath() {
  const value = new URLSearchParams(window.location.search).get("next");
  if (!value?.startsWith("/") || value.startsWith("//") || value.includes("\\")) return "/";
  const destination = new URL(value, window.location.origin);
  return destination.origin === window.location.origin
    ? `${destination.pathname}${destination.search}${destination.hash}`
    : "/";
}

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, loading, login, register } = useAuth();
  const [mode, setMode] = useState<Mode>("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [googleEnabled, setGoogleEnabled] = useState(false);
  const oauthMessage = searchParams.get("oauth_error") ?? searchParams.get("error");
  const oauthError = !oauthMessage ? null : oauthMessage === "access_denied"
    ? "Cancelaste el acceso con Google. Puedes intentarlo de nuevo."
    : "No pudimos completar el acceso con Google. Inténtalo nuevamente.";

  useEffect(() => {
    if (!loading && user && !submitting) router.replace(safeNextPath());
  }, [loading, router, submitting, user]);

  useEffect(() => {
    const controller = new AbortController();
    authApi.providers(controller.signal)
      .then((providers) => setGoogleEnabled(providers.google))
      .catch(() => setGoogleEnabled(false));
    return () => controller.abort();
  }, []);

  function changeMode(nextMode: Mode) {
    setMode(nextMode);
    setError(null);
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (password.length < 12 || password.length > 72) {
      setError("La contraseña debe tener entre 12 y 72 caracteres.");
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      if (mode === "login") await login({ email, password });
      else await register({ name, email, password });
      router.replace(safeNextPath());
    } catch (requestError) {
      setError(requestError instanceof ApiError && requestError.status === 401 && mode === "login"
        ? "El correo o la contraseña no son correctos."
        : requestError instanceof Error ? requestError.message : "No pudimos iniciar tu sesión.");
    } finally {
      setSubmitting(false);
    }
  }

  function startGoogleLogin() {
    window.location.assign(new URL("/backend/oauth2/authorization/google", window.location.origin));
  }

  return (
    <main className="relative min-h-screen min-h-dvh overflow-hidden px-4 py-6 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_18%_12%,rgba(255,255,255,0.1),transparent_28rem)]" />
      <div className="relative mx-auto grid min-h-[calc(100dvh-3rem)] max-w-6xl items-center gap-12 lg:grid-cols-[1.08fr_0.92fr]">
        <section className="hidden max-w-xl lg:block" aria-label="Introducción a Ariscend">
          <p className="mb-12 text-4xl font-bold tracking-[-0.055em]">Ariscend</p>
          <div className="mb-8 flex size-14 items-center justify-center rounded-2xl border border-white/10 bg-white/[0.04]">
            <LockKeyhole className="size-6" aria-hidden="true" />
          </div>
          <p className="font-[var(--font-geist)] text-xs font-semibold uppercase tracking-[0.2em] text-[#8c8e91]">Tu espacio privado</p>
          <h1 className="mt-4 text-6xl font-semibold leading-[0.98] tracking-[-0.06em]">Orden para avanzar.<br />Privacidad para enfocarte.</h1>
          <p className="mt-6 max-w-lg text-lg leading-8 text-[#a9abad]">Hábitos, pendientes, notas y finanzas reunidos en un espacio que solo tú puedes abrir.</p>
        </section>

        <section className="animate-enter mx-auto w-full max-w-md">
          <p className="mb-10 text-3xl font-bold tracking-[-0.05em] lg:hidden">Ariscend</p>
          <div className="glass-panel rounded-[1.75rem] p-6 shadow-2xl shadow-black sm:p-9">
            <div className="mb-8">
              <p className="mb-2 font-[var(--font-geist)] text-xs font-semibold uppercase tracking-[0.18em] text-[#8c8e91]">Acceso seguro</p>
              <h1 className="text-3xl font-semibold tracking-[-0.045em] sm:text-4xl">{mode === "login" ? "Bienvenido de vuelta" : "Crea tu cuenta"}</h1>
              <p className="mt-3 leading-6 text-[#a9abad]">{mode === "login" ? "Continúa donde dejaste tu progreso." : "Un único lugar para construir tu sistema personal."}</p>
            </div>

            <div className="mb-6 grid grid-cols-2 rounded-full border border-white/[0.08] bg-black/40 p-1" role="group" aria-label="Tipo de acceso">
              <button type="button" onClick={() => changeMode("login")} aria-pressed={mode === "login"} className={`focus-ring h-10 rounded-full text-sm font-semibold transition ${mode === "login" ? "bg-white text-black" : "text-[#a9abad] hover:text-white"}`}>Ingresar</button>
              <button type="button" onClick={() => changeMode("register")} aria-pressed={mode === "register"} className={`focus-ring h-10 rounded-full text-sm font-semibold transition ${mode === "register" ? "bg-white text-black" : "text-[#a9abad] hover:text-white"}`}>Registrarme</button>
            </div>

            {(oauthError || error) && <div role="alert" className="mb-5 rounded-xl border border-red-300/20 bg-red-950/25 px-4 py-3 text-sm leading-5 text-red-200">{oauthError ?? error}</div>}

            <form onSubmit={submit} className="space-y-4">
              {mode === "register" && <label className="block text-sm font-medium text-[#c7c8ca]">Nombre
                <input required autoComplete="name" maxLength={100} value={name} onChange={(event) => setName(event.target.value)} className="focus-ring mt-2 h-12 w-full rounded-xl border border-white/10 bg-black/70 px-4 text-white placeholder:text-white/25" placeholder="Tu nombre" />
              </label>}
              <label className="block text-sm font-medium text-[#c7c8ca]">Correo electrónico
                <input required type="email" autoComplete="email" maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} className="focus-ring mt-2 h-12 w-full rounded-xl border border-white/10 bg-black/70 px-4 text-white placeholder:text-white/25" placeholder="tu@correo.com" />
              </label>
              <label className="block text-sm font-medium text-[#c7c8ca]">Contraseña
                <span className="relative mt-2 block">
                  <input required type={showPassword ? "text" : "password"} autoComplete={mode === "login" ? "current-password" : "new-password"} minLength={12} maxLength={72} value={password} onChange={(event) => setPassword(event.target.value)} aria-describedby="password-help" className="focus-ring h-12 w-full rounded-xl border border-white/10 bg-black/70 px-4 pr-12 text-white placeholder:text-white/25" placeholder="12 caracteres como mínimo" />
                  <button type="button" onClick={() => setShowPassword((visible) => !visible)} className="focus-ring absolute right-1 top-1 flex size-10 items-center justify-center rounded-lg text-[#8c8e91] hover:text-white" aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}>{showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}</button>
                </span>
                <span id="password-help" className="mt-2 block text-xs text-[#737579]">Entre 12 y 72 caracteres.</span>
              </label>
              <Button type="submit" disabled={submitting || loading} className="mt-2 w-full">
                {submitting ? "Verificando..." : mode === "login" ? "Ingresar" : "Crear cuenta"}
                {!submitting && <ArrowRight className="size-4" aria-hidden="true" />}
              </Button>
            </form>

            {googleEnabled && <>
              <div className="my-6 flex items-center gap-3 text-xs uppercase tracking-[0.15em] text-[#8c8e91]"><span className="h-px flex-1 bg-white/[0.08]" />o continúa con<span className="h-px flex-1 bg-white/[0.08]" /></div>
              <Button type="button" variant="secondary" onClick={startGoogleLogin} className="w-full"><span className="font-[var(--font-geist)] text-base font-bold" aria-hidden="true">G</span> Google</Button>
            </>}
          </div>
          <p className="mt-5 text-center text-xs leading-5 text-[#a9abad]">Al continuar, aceptas mantener un uso responsable de tu cuenta.</p>
        </section>
      </div>
    </main>
  );
}

export default function LoginPage() {
  return <Suspense><LoginForm /></Suspense>;
}

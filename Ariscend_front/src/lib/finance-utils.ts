export function localDateKey(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function currentMonthRange() {
  const now = new Date();
  return {
    from: localDateKey(new Date(now.getFullYear(), now.getMonth(), 1)),
    to: localDateKey(new Date(now.getFullYear(), now.getMonth() + 1, 0)),
  };
}

export function formatMoney(amount: number | null | undefined, currency: string) {
  return new Intl.NumberFormat("es-MX", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(amount ?? 0);
}

export const inputClass =
  "focus-ring h-11 w-full rounded-xl border border-white/10 bg-black px-3.5 text-sm text-white placeholder:text-white/25";

export const labelClass = "block text-sm text-[#a9abad]";

export const currencyOptions = [
  { code: "MXN", label: "Peso mexicano" },
  { code: "USD", label: "Dólar estadounidense" },
  { code: "EUR", label: "Euro" },
  { code: "CAD", label: "Dólar canadiense" },
  { code: "GBP", label: "Libra esterlina" },
  { code: "COP", label: "Peso colombiano" },
  { code: "ARS", label: "Peso argentino" },
  { code: "CLP", label: "Peso chileno" },
  { code: "PEN", label: "Sol peruano" },
] as const;

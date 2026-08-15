"use client";

import { useEffect, useState } from "react";
import { ArrowDownRight, ArrowUpRight, CalendarDays, RefreshCw, Scale } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ObsidianCard } from "@/components/ui/obsidian-card";
import { PageLoading } from "@/components/feedback/page-loading";
import { useSelectedUser } from "@/contexts/selected-user-context";
import { currentMonthRange, formatMoney, inputClass } from "@/lib/finance-utils";
import { financeApi } from "@/services/finance-api";
import type { FinanceSummary } from "@/types/api";

export function FinanceOverview() {
  const { selectedUser } = useSelectedUser();
  const initialRange = currentMonthRange();
  const [dateFrom, setDateFrom] = useState(initialRange.from);
  const [dateTo, setDateTo] = useState(initialRange.to);
  const [summary, setSummary] = useState<FinanceSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    if (!selectedUser) return;
    let active = true;
    financeApi.summary(selectedUser.id, dateFrom, dateTo)
      .then((result) => { if (active) setSummary(result); })
      .catch((error) => { if (active) toast.error(error instanceof Error ? error.message : "No se pudo cargar el resumen."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [dateFrom, dateTo, reloadKey, selectedUser]);

  if (loading && !summary) return <PageLoading label="Calculando tu balance" />;
  if (!summary) {
    return <ObsidianCard className="flex min-h-56 flex-col items-center justify-center gap-4 p-7"><p className="text-[#999b9e]">No pudimos calcular este periodo.</p><Button variant="secondary" onClick={() => setReloadKey((value) => value + 1)}><RefreshCw className="size-4" /> Reintentar</Button></ObsidianCard>;
  }

  const maxCategory = Math.max(1, ...Object.values(summary.expensesByCategory));

  return (
    <div className="space-y-5">
      <ObsidianCard className="p-5 md:p-6">
        <div className="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div><p className="text-xs font-semibold uppercase tracking-[0.14em] text-[#85878a]">Periodo del resumen</p><p className="mt-2 text-sm text-[#a9abad]">Todos los importes se convierten a {summary.baseCurrency}.</p></div>
          <div className="grid grid-cols-2 gap-2">
            <label className="text-xs text-[#85878a]">Desde<input type="date" value={dateFrom} onChange={(event) => setDateFrom(event.target.value)} className={`${inputClass} mt-1`} /></label>
            <label className="text-xs text-[#85878a]">Hasta<input type="date" value={dateTo} onChange={(event) => setDateTo(event.target.value)} className={`${inputClass} mt-1`} /></label>
          </div>
        </div>
      </ObsidianCard>

      <div className="grid gap-4 md:grid-cols-3">
        <ObsidianCard className="p-6 md:col-span-3 md:p-8">
          <div className="relative z-10"><div className="flex items-center gap-2 text-[#929497]"><Scale className="size-4" /><p className="text-xs font-semibold uppercase tracking-[0.14em]">Balance neto</p></div><p className="mt-4 text-4xl font-semibold tracking-[-0.05em] md:text-6xl">{formatMoney(summary.balance, summary.baseCurrency)}</p><p className="mt-3 text-sm text-[#7d7f82]">{summary.dateFrom} — {summary.dateTo}</p></div>
        </ObsidianCard>
        <ObsidianCard className="p-5"><div className="relative z-10"><ArrowUpRight className="size-5 text-[#b8babc]" /><p className="mt-7 text-sm text-[#8c8e91]">Ingresos</p><p className="mt-1 text-2xl font-semibold">{formatMoney(summary.totalIncome, summary.baseCurrency)}</p></div></ObsidianCard>
        <ObsidianCard className="p-5"><div className="relative z-10"><ArrowDownRight className="size-5 text-[#b8babc]" /><p className="mt-7 text-sm text-[#8c8e91]">Gastos</p><p className="mt-1 text-2xl font-semibold">{formatMoney(summary.totalExpenses, summary.baseCurrency)}</p></div></ObsidianCard>
        <ObsidianCard className="p-5"><div className="relative z-10"><CalendarDays className="size-5 text-[#b8babc]" /><p className="mt-7 text-sm text-[#8c8e91]">Monedas usadas</p><p className="mt-1 text-2xl font-semibold">{Object.keys(summary.originalTotalsByCurrency).length}</p></div></ObsidianCard>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <ObsidianCard className="p-5 md:p-6">
          <div className="relative z-10"><h3 className="text-lg font-semibold">Gastos por categoría</h3><div className="mt-6 space-y-4">{Object.entries(summary.expensesByCategory).length === 0 ? <p className="text-sm text-[#85878a]">Todavía no hay gastos en este periodo.</p> : Object.entries(summary.expensesByCategory).sort((a, b) => b[1] - a[1]).map(([name, amount]) => <div key={name}><div className="mb-2 flex justify-between gap-3 text-sm"><span className="truncate text-[#b7b9bb]">{name}</span><span className="font-[var(--font-geist)]">{formatMoney(amount, summary.baseCurrency)}</span></div><div className="h-1.5 rounded-full bg-white/[0.07]"><div className="h-full rounded-full bg-white" style={{ width: `${Math.max(3, (amount / maxCategory) * 100)}%` }} /></div></div>)}</div></div>
        </ObsidianCard>
        <ObsidianCard className="p-5 md:p-6">
          <div className="relative z-10"><h3 className="text-lg font-semibold">Totales originales</h3><p className="mt-2 text-sm text-[#85878a]">Antes de convertir a la moneda base.</p><div className="mt-6 space-y-3">{Object.entries(summary.originalTotalsByCurrency).length === 0 ? <p className="text-sm text-[#85878a]">Sin movimientos.</p> : Object.entries(summary.originalTotalsByCurrency).map(([currency, values]) => <div key={currency} className="rounded-xl border border-white/[0.07] p-4"><div className="flex justify-between"><span className="font-semibold">{currency}</span><span className="text-sm text-[#85878a]">Balance {formatMoney(values.income - values.expenses, currency)}</span></div><div className="mt-3 grid grid-cols-2 gap-3 text-sm"><span className="text-[#9b9da0]">Ingresos<br /><b className="text-white">{formatMoney(values.income, currency)}</b></span><span className="text-[#9b9da0]">Gastos<br /><b className="text-white">{formatMoney(values.expenses, currency)}</b></span></div></div>)}</div></div>
        </ObsidianCard>
      </div>
    </div>
  );
}
